---
name: clock-in
description: Start a working session by establishing the real state of the repository and picking up a topic from progress/. Use at the beginning of a day, when resuming after an interruption, or when starting a new topic that will span more than one session.
---

# Clock In Skill

## Read the toolkit skill first

The ritual is the toolkit's: `skills/clock-in/SKILL.md`, resolved through the
lookup order in this repository's `AGENTS.md`. It owns establishing state before
asking anything, the single topic question, the two layers of a working day, the
session lead rule, and how the private journal binding is resolved.

This file adds only what is specific to this repository. Where the toolkit
describes a default, the paths below narrow it; nothing here restates a toolkit
rule.

## What this project fills in

**Topics live in `progress/<slug>.adoc`.** One file per topic, AsciiDoc, from the
toolkit's `skills/clock-in/templates/progress.adoc`. `progress/` is working
state, not documentation: it sits outside `docs/`, carries no metamodel front
matter, and the validator does not read it.

**The project diary is `diary/`,** one file per working day, and
`skills/diary/SKILL.md` owns what an entry contains and where it goes. At
clock-in the relevant file is the newest one — it is often why the plan says
what it says.

**The long-lived thread list is `diary/open-threads.adoc`.** A thread that
outlives its topic goes there; one that dies with the topic stays in the
progress file.

**Every artifact this project produces has a register.**
`skills/language-profile/SKILL.md` decides how a text reads — profile 5 for the
diary — and applies before anything is written, not only when something looks
like a style question.

**The checks command is `./build.sh all`.** It goes into the progress file's
`Checks` section as a command; a number produced by running it does not.

## When this skill should be deleted

If `progress/`, the AsciiDoc shape, and the diary wiring ever stop being
specific to this repository, nothing is left here that the toolkit does not
already own, and this file goes.

## Supporting files

The progress template is the toolkit's, referenced rather than copied. There is
no local copy to drift.
