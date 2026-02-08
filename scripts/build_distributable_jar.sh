#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

resolve_latest_module_jar() {
  local group="$1"
  local artifact="$2"
  find "$HOME/.gradle/caches/modules-2/files-2.1/$group/$artifact" -name '*.jar' 2>/dev/null | sort -V | tail -n1
}

require_file() {
  local path="$1"
  local label="$2"
  if [[ -z "$path" || ! -f "$path" ]]; then
    echo "Missing $label: $path" >&2
    exit 1
  fi
}

./gradlew --no-daemon clean remapJar -x test

remap_jar="$(ls -1t build/libs/create-fabric-*.jar | head -n1)"
dev_jar="$(ls -1t build/devlibs/create-fabric-*-dev.jar | head -n1)"

require_file "$remap_jar" "remapped jar"
require_file "$dev_jar" "dev jar"

mapping_hash="$(tr ':' '\n' < .gradle/loom-cache/remapClasspath.txt \
  | sed -n 's#.*layered+hash\.\([0-9]\+\)-v2.*#\1#p' \
  | tail -n1)"

mappings_file=""
named_mc_cp=""
if [[ -n "$mapping_hash" ]]; then
  mappings_file="$HOME/.gradle/caches/fabric-loom/1.21.1/loom.mappings.1_21_1.layered+hash.${mapping_hash}-v2/mappings.tiny"
  named_mc_cp="$(find .gradle/loom-cache/minecraftMaven/net/minecraft \
    -path "*1.21.1-loom.mappings.1_21_1.layered+hash.${mapping_hash}-v2*" \
    -name "minecraft-merged-*-1.21.1-loom.mappings.1_21_1.layered+hash.${mapping_hash}-v2.jar" \
    | head -n1)"
fi

if [[ -z "$mappings_file" || ! -f "$mappings_file" ]]; then
  mappings_file="$(find "$HOME/.gradle/caches/fabric-loom/1.21.1" -path '*loom.mappings.1_21_1.layered+hash.*-v2/mappings.tiny' | sort -V | tail -n1)"
fi

if [[ -z "$named_mc_cp" || ! -f "$named_mc_cp" ]]; then
  named_mc_cp="$(find .gradle/loom-cache/minecraftMaven/net/minecraft \
    -path '*1.21.1-loom.mappings.1_21_1.layered+hash.*-v2*' \
    -name 'minecraft-merged-*-1.21.1-loom.mappings.1_21_1.layered+hash.*-v2.jar' \
    | sort -V | tail -n1)"
fi

require_file "$mappings_file" "mappings.tiny"
require_file "$named_mc_cp" "named minecraft classpath jar"

readarray -t remapped_mod_classpath < <(find .gradle/loom-cache/remapped_mods -name '*.jar' | sort)
if [[ ${#remapped_mod_classpath[@]} -eq 0 ]]; then
  echo "Missing remapped mod classpath jars under .gradle/loom-cache/remapped_mods" >&2
  exit 1
fi

tiny_remapper_jar="$(resolve_latest_module_jar net.fabricmc tiny-remapper)"
asm_jar="$(resolve_latest_module_jar org.ow2.asm asm)"
asm_commons_jar="$(resolve_latest_module_jar org.ow2.asm asm-commons)"
asm_tree_jar="$(resolve_latest_module_jar org.ow2.asm asm-tree)"
asm_util_jar="$(resolve_latest_module_jar org.ow2.asm asm-util)"
mapping_io_jar="$(resolve_latest_module_jar net.fabricmc mapping-io)"

require_file "$tiny_remapper_jar" "tiny-remapper jar"
require_file "$asm_jar" "asm jar"
require_file "$asm_commons_jar" "asm-commons jar"
require_file "$asm_tree_jar" "asm-tree jar"
require_file "$asm_util_jar" "asm-util jar"
require_file "$mapping_io_jar" "mapping-io jar"

tr_classpath="$tiny_remapper_jar:$asm_jar:$asm_commons_jar:$asm_tree_jar:$asm_util_jar:$mapping_io_jar"

remapped_classes_jar="build/tmp/remapIgnoreConflicts.jar"
rm -f "$remapped_classes_jar"

java -cp "$tr_classpath" net.fabricmc.tinyremapper.Main \
  "$dev_jar" \
  "$remapped_classes_jar" \
  "$mappings_file" \
  named intermediary \
  "$named_mc_cp" \
  "${remapped_mod_classpath[@]}" \
  --ignoreConflicts

require_file "$remapped_classes_jar" "tiny-remapper output jar"

merge_a="build/tmp/merge_base"
merge_b="build/tmp/merge_remapped"
merge_out="build/tmp/merge_out"
merged_jar="build/tmp/create-fabric-merged.jar"

rm -rf "$merge_a" "$merge_b" "$merge_out" "$merged_jar"
mkdir -p "$merge_a" "$merge_b" "$merge_out"

(cd "$merge_a" && jar xf "$ROOT_DIR/$remap_jar")
(cd "$merge_b" && jar xf "$ROOT_DIR/$remapped_classes_jar")
cp -a "$merge_a/." "$merge_out/"

while IFS= read -r class_file; do
  if [[ -f "$merge_b/$class_file" ]]; then
    cp "$merge_b/$class_file" "$merge_out/$class_file"
  fi
done < <(cd "$merge_a" && find . -type f -name '*.class' | sed 's#^./##')

(cd "$merge_out" && jar cf "$ROOT_DIR/$merged_jar" .)
mv "$merged_jar" "$remap_jar"

echo "Distributable jar ready: $remap_jar"
