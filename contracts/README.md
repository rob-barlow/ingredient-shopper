# Contracts

[`openapi.yaml`](openapi.yaml) is **the** API contract between the frontend and the backend (constitution Article III).

| Rule | Where it's written down |
|---|---|
| Written and agreed **before** the code | Article III |
| Versioned, with a changelog | Article IIIa, [CHANGELOG.md](CHANGELOG.md) |
| The backend's Spring interfaces are **generated** from it | ADR-013 (T-004) |
| The frontend's C# client is **generated** from it | ADR-014 (T-006) |
| Every API test response is **validated** against it | ADR-016 (T-007) |

## Validating

```powershell
npx @redocly/cli@latest lint --config contracts/redocly.yaml contracts/openapi.yaml
```

- Rules live in [`redocly.yaml`](redocly.yaml). Deliberate exceptions, each with its reason, live in [`.redocly.lint-ignore.yaml`](.redocly.lint-ignore.yaml).
- **Every operation must declare its security:** `security: []` for public, or `bearerAuth` for admin. The linter enforces this.
- CI runs the lint too (T-008).
- *On Windows, if `npx` fails with a certificate error, set `$env:NODE_OPTIONS="--use-system-ca"` first.*

## Changing the contract

1. Edit `openapi.yaml` and add a line under **Unreleased** in `CHANGELOG.md`.
2. Lint it (above).
3. Rebuild the backend and frontend. The compiler shows everything that needs updating.
4. Use the PR checklist item: *"Contract and its CHANGELOG updated"*.
