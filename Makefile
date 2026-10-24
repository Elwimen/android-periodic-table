# Periodic Table device helpers.
#
#   make install                 build (if needed) and install on every connected device
#   make install DEVICE=<serial> install on one device
#   make run                     install, then launch the app
#   make build                   build the APK
#   make devices                 list connected devices with model and ABIs
#   make uninstall               uninstall the app from every connected device
#
# This app has no native code, so a SINGLE APK runs on both 32-bit and 64-bit
# devices -- there are no per-ABI splits to build (unlike projects that ship
# native .so libraries). `make devices` still reports each device's ABI list.
#
# Build type: debug by default. Pass BUILD=release for the signed release
# (needs keystore.properties). Note a debug APK and a release APK are signed
# with different keys, so you cannot install one over the other -- `make
# uninstall` first, or stay on one build type per device.
#
#   make install BUILD=release   build + install the signed release
#   make install REBUILD=1       force a rebuild even if the APK exists

SHELL := /bin/bash
.SHELLFLAGS := -eu -o pipefail -c
.ONESHELL:
.SILENT:

ADB     ?= $(or $(wildcard $(HOME)/Android/Sdk/platform-tools/adb),adb)
PACKAGE := com.frozendevs.periodictable
BUILD   ?= debug
APK      = PeriodicTable/build/outputs/apk/$(BUILD)/PeriodicTable-$(BUILD).apk
DEVICE  ?=
REBUILD ?=

ifeq ($(BUILD),release)
  GRADLE_TASK := assembleRelease
else ifeq ($(BUILD),debug)
  GRADLE_TASK := assembleDebug
else
  $(error BUILD must be 'debug' or 'release', got '$(BUILD)')
endif

.PHONY: build install run devices uninstall

build:
	./gradlew $(GRADLE_TASK)

devices:
	for s in $$("$(ADB)" devices | awk 'NR>1 && $$2=="device" {print $$1}'); do
	  model=$$("$(ADB)" -s $$s shell getprop ro.product.model | tr -d '\r')
	  abilist=$$("$(ADB)" -s $$s shell getprop ro.product.cpu.abilist | tr -d '\r')
	  echo "$$s  $$model  [$$abilist]"
	done
	"$(ADB)" devices | awk 'NR>1 && $$2!="device" && NF {print $$1 "  (" $$2 ")"}'

# Resolve the target serials: DEVICE=... if given, else every authorized device.
define RESOLVE_SERIALS
	if [ -n "$(DEVICE)" ]; then
	  serials="$(DEVICE)"
	else
	  "$(ADB)" devices | awk 'NR>1 && $$2!="device" && NF {print "skipping " $$1 " (" $$2 ")"}' >&2
	  serials=$$("$(ADB)" devices | awk 'NR>1 && $$2=="device" {print $$1}')
	fi
	if [ -z "$$serials" ]; then echo "no authorized device connected" >&2; exit 1; fi
endef

install:
	if [ -n "$(REBUILD)" ] || [ ! -f "$(APK)" ]; then ./gradlew $(GRADLE_TASK); fi
	$(RESOLVE_SERIALS)
	failed=""
	for s in $$serials; do
	  model=$$("$(ADB)" -s $$s shell getprop ro.product.model | tr -d '\r')
	  echo "==> $$s ($$model): $(BUILD)"
	  if ! "$(ADB)" -s $$s install -r -g "$(APK)"; then failed="$$failed $$s"; continue; fi
	  if [ -n "$(LAUNCH)" ]; then
	    "$(ADB)" -s $$s shell monkey -p $(PACKAGE) -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
	    echo "    launched"
	  fi
	done
	if [ -n "$$failed" ]; then
	  echo "install failed on:$$failed" >&2
	  echo "(a signature clash means the other build type is installed -- 'make uninstall' first)" >&2
	  exit 1
	fi

run:
	$(MAKE) --no-print-directory install LAUNCH=1

uninstall:
	$(RESOLVE_SERIALS)
	for s in $$serials; do
	  if "$(ADB)" -s $$s shell pm list packages | tr -d '\r' | grep -qx "package:$(PACKAGE)"; then
	    echo "==> $$s: uninstall $(PACKAGE)"
	    "$(ADB)" -s $$s uninstall $(PACKAGE) >/dev/null && echo "    done"
	  else
	    echo "==> $$s: $(PACKAGE) not installed, skipping"
	  fi
	done
