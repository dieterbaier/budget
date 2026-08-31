---
name: clock-out
description: End a working session by refreshing the progress files for the topics touched and writing the diary entry for the day. Use when the owner calls it a day, before a long break, or when a topic is being put on ice.
---

# Clock Out Skill

## Read the toolkit skill first

The ritual is the toolkit's: `skills/clock-out/SKILL.md`, resolved through the
lookup order in this repository's `AGENTS.md`. It owns closing the loop on the
work itself, how a progress file is refreshed, the session lead rule, and the
handover record that carries the day up into the private journal.

This file adds only what is specific to this repository.

## What this project fills in

**The two artifacts are `progress/<topic>.adoc` and `diary/YYYY-MM-DD-<slug>.adoc`.**
The toolkit states the split — where we are versus what we learned, and that a
sentence fitting both belongs in the diary. The paths, the AsciiDoc shape, and
the day-file lifecycle are this repository's.

**`skills/diary/SKILL.md` owns the diary entry:** what it contains, where it
goes, and which of the cross-cutting documents must grow today.
`skills/language-profile/SKILL.md` profile 5 owns how it reads. Read both before
writing; do not reconstruct their rules from the toolkit's step 4.

**Close the day, or do not, and say which.** A day file is open until the day it
covers is closed and append-only afterwards. A later session needs to know
whether it may still edit the file, and only this repository's diary works that
way.

**Threads that outlive their topic go to `diary/open-threads.adoc`;** one that
dies with the topic stays in the progress file.

**What goes up to the private journal is the summary, not the narrative.** The
diary entry stays here. The handover record carries the verified status line,
the findings that are not specific to this project, the evidence links, and the
threads that outlive it.

## When this skill should be deleted

If the two artifacts, their paths, and the day-file lifecycle ever stop being
specific to this repository, nothing is left here that the toolkit does not
already own, and this file goes.

## Supporting files

The progress template is the toolkit's, referenced rather than copied. There is
no local copy to drift.
