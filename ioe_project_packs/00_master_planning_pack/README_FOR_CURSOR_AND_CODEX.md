# Cursor / Codex Usage

1. Open one module folder at a time.
2. Paste the module `CODEX_IMPLEMENTATION_PROMPT.txt` into Codex.
3. Tell Codex to read all files in `docs/` before editing.
4. Build `ioe_core` first.
5. Do not let Codex implement another module inside the current module.
6. Ask Codex for build evidence and changed-file summary at the end.

Recommended order:

```text
01_ioe_core
02_ioe_expedition_worldgen
05_ioe_ieip_prospecting
03_ioe_crystal_growth
04_ioe_nether_geodes
06_ioe_retrogen_admin
```
