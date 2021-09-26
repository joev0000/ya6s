#!/usr/bin/env bash
for f in *.s; do
  ca65 -t none -l >(cat) $f
done
echo
ld65 -m >(cat) -C ld65.config *.o
