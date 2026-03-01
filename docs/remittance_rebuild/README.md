# Remittance Rebuild Archive

This folder captures the exact last committed source for the remittance feature files that were removed from Clearr.

Source baseline:
- commit: `fdc9a50`

Files are split by layer so another AI can rebuild the feature without ingesting one oversized document.

Included documents:
- `data_layer.md`
- `domain_layer.md`
- `ui_layer.md`
- `tests.md`

Notes:
- Content is copied from git `HEAD` at the time the remittance files still existed in the commit tree.
- These docs intentionally preserve the original package names and file paths.
- Shared files that were modified but not deleted are not duplicated here.
- `shared_integration_pre_removal.md`
- `shared_integration_diff.md`

Additional notes:
- `shared_integration_pre_removal.md` contains exact pre-removal implementations for shared files that still existed outside the deleted remittance folders.
- `shared_integration_diff.md` shows the removal diff against the current working tree, which helps another AI understand what changed when remittance was stripped out.
