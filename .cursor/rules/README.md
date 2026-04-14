# FinTrack — Cursor Rules Setup

This folder contains AI coding rules for the FinTrack project.
They are automatically applied by Cursor IDE during code generation.

## What's in here

| File | Applies to | Purpose |
|---|---|---|
| `angular-typescript.mdc` | `**/*.ts`, `**/*.html` | Angular 19 + TypeScript best practices |
| `tailwind-ui.mdc` | `**/*.html`, `**/*.css` | Tailwind v4 UI patterns and design system enforcement |
| `project-standards.mdc` | `**/*` | Naming, API contracts, git, security, performance |

## Sources

Rules are synthesised from:
- [`PatrickJS/awesome-cursorrules`](https://github.com/PatrickJS/awesome-cursorrules) — angular-typescript, tailwind-react, tailwind-nextjs rule files
- [`danhollick/tailwind-css-v4`](https://gist.github.com/danhollick/d902cf60e37950de36cf8e7c43fa0943) — Tailwind v4-specific rules
- FinTrack `FRONTEND_REQUIREMENTS.md` design system — component patterns and class conventions

## How Cursor uses these

Cursor reads all `.mdc` files in `.cursor/rules/` automatically.
The `globs` field in each file's frontmatter controls which files trigger the rule.
`alwaysApply: true` means the rule is included in every prompt regardless of file.

## How to use with other AI tools (Claude, ChatGPT, Copilot)

If you are not using Cursor, paste the relevant rule file content
at the top of your prompt before describing the task. Example:

```
[paste contents of angular-typescript.mdc]
[paste contents of tailwind-ui.mdc]

Now build the TransactionListComponent as described in FRONTEND_REQUIREMENTS.md section 6.5.
```

## Updating the rules

Edit the `.mdc` files directly. Commit changes to git so the whole team
(and your AI agent) stays in sync.
