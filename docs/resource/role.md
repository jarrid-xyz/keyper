---
title: Role
hide:
  - toc
---

## Create

Create new role configurations.

```bash
keyper resource create -t role -n test-role
```

This will create a role configuration file in the `configs/<deployment name>/role/<uuid>` directory:

```json
{
  "base": {
    "created": "<timestamp>",
    "id": "<uuid>",
    "name": "test-role"
  },
  "type": "ROLE"
}
```

## List

List existing role configurations.

```bash
keyper resource list -t role

# Roles:
# test-role
```
