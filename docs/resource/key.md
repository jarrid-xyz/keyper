---
title: Key
hide:
  - toc
---

## Create

Create new key configurations.

```bash
keyper resource create -t key
```

This will create a key configuration file in the `configs/<deployment name>/key/<uuid>` directory:

```json
{
  "base": {
    "created": "<timestamp>",
    "id": "<uuid>"
  },
  "type": "KEY"
}
```

## List

List existing key configurations.

```bash
keyper resource list -t key

# Keys:
# 4ae00cac-5a73-44dc-a6c7-83f8cc35ad23
# 68618138-019a-4fb8-ab98-93aa07c0c984
```

## Create Permission

Now, you can allow existing [roles](role.md) to encrypt or decrypt with existing key.

```bash
keyper resource key -k <key-id> -o ADD_ALLOW_ENCRYPT -r test-role
keyper resource key -k <key-id> -o ADD_ALLOW_DECRYPT -r test-role
```

```json
{
    "base": {
        "created": "<timestamp>",
        "updated": "<timestamp>",
        "id": "<uuid>",
    },
    "type": "KEY",
    "ttl": 7,
    "rotationPeriod": "7776000s",
    "permission": {
        "allowEncrypt": [
            "test-role"
        ],
        "allowDecrypt": [
            "test-role"
        ]
    }
}
```

Once the key configuration is updated, when you run [`keyper deploy plan`](../deploy/cli.md/#plan), you will see iam and key policy created automatically.

```bash
keyper deploy plan

#    ~ resource "google_kms_crypto_key_iam_policy" "jarrid-keyper-key-policy-<key-id>" {
#          id            = "projects/xxx/locations/us-east1/keyRings/default/cryptoKeys/jarrid-keyper-key-<key-id>"
#        ~ policy_data   = jsonencode(
#            ~ {
#                ~ bindings = [
#                    + {
#                        + members = [
#                            + "serviceAccount:test-role@xxx.iam.gserviceaccount.com",
#                          ]
#                        + role    = "roles/cloudkms.cryptoKeyDecrypter"
#                      },
#                      {
#                          members = [
#                              "serviceAccount:test-role@xxx.iam.gserviceaccount.com",
#                          ]
#                          role    = "roles/cloudkms.cryptoKeyEncrypter"
#                      },
#                  ]
#              }
#          )
#          # (2 unchanged attributes hidden)
#      }

#  Plan: 0 to add, 1 to change, 0 to destroy.

```

## Demo

<script src="https://asciinema.org/a/669493.js" id="asciicast-669493" async="true"></script>

