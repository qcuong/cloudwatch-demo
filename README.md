# Create IAM Role for CloudWatch Logs to sends log data to KDS streams

```bash
aws iam create-role --role-name CWLtoKinesisRole --assume-role-policy-document file://src/main/resources/iam-role/TrustPolicyForCWL-Kinesis.json
aws iam put-role-policy  --role-name CWLtoKinesisRole  --policy-name Permissions-Policy-For-CWL  --policy-document file://src/main/resources/iam-role/PermissionsForCWL-Kinesis.json
```