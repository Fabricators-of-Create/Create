#!/usr/bin/env bash

set -euo pipefail

if [[ $# -ne 1 ]]; then
	echo "Usage: $0 <log-file>"
	exit 2
fi

log_file="$1"
if [[ ! -f "$log_file" ]]; then
	echo "Log file not found: $log_file"
	exit 2
fi

clean_log="$(mktemp)"
trap 'rm -f "$clean_log"' EXIT

# Strip ANSI color/control sequences to make pattern matching deterministic.
sed -r 's/\x1B\[[0-9;]*[A-Za-z]//g' "$log_file" > "$clean_log"

actionable_patterns=(
	"Task :run(Client|Server) FAILED"
	"BUILD FAILED"
	"Game crashed"
	"Reported exception thrown"
	"StackOverflowError"
	"Couldn't load tag"
	"TranslationConventionLogWarnings"
	"ConventionLogWarnings"
	"Untranslated item tags"
	"Legacy Tags"
	"Suspicious fluid amount in recipe"
)

external_patterns=(
	"Reference map 'ponder.refmap.json' for ponder.mixins.json could not be read"
	"Reference map 'forgeconfigapiport.common.refmap.json' for forgeconfigapiport.common.mixins.json could not be read"
	"Error loading class: io/vram/frex/base/renderer/context/render/EntityBlockRenderContext"
	"@Mixin target io.vram.frex.base.renderer.context.render.EntityBlockRenderContext was not found"
	"Error loading class: mezz/jei/library/load/PluginCaller"
	"Error loading class: net/minecraft/client/renderer/entity/BoatRenderer"
	"Error loading class: net/minecraft/client/gui/screens/worldselection/CreateWorldScreen"
	"Error loading class: net/minecraft/client/renderer/entity/FishingHookRenderer"
	"Error loading class: net/minecraft/client/multiplayer/ClientLevel"
	"Missing sound for event: minecraft:item.goat_horn.play"
	"Missing sound for event: minecraft:entity.goat.screaming.horn_break"
	"Shader rendertype_entity_translucent_emissive could not find sampler named Sampler2"
)

print_section() {
	local title="$1"
	shift
	local -a patterns=("$@")
	local total=0

	echo "== $title =="
	for pattern in "${patterns[@]}"; do
		local count
		count="$(rg -c --no-messages "$pattern" "$clean_log" || true)"
		if [[ "${count:-0}" -gt 0 ]]; then
			total=$((total + count))
			echo "[$count] $pattern"
		fi
	done

	if [[ $total -eq 0 ]]; then
		echo "(none)"
	fi
	echo

	return 0
}

print_section "Actionable (mod/data issues)" "${actionable_patterns[@]}"
print_section "Known External/Benign (dependency/runtime noise)" "${external_patterns[@]}"

actionable_total=0
for pattern in "${actionable_patterns[@]}"; do
	count="$(rg -c --no-messages "$pattern" "$clean_log" || true)"
	actionable_total=$((actionable_total + count))
done

if [[ $actionable_total -gt 0 ]]; then
	echo "Result: ACTION REQUIRED ($actionable_total actionable matches)"
	exit 1
fi

echo "Result: CLEAN (no actionable warning signatures detected)"
