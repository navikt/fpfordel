#!/bin/sh

set -eu

# Set AWS credentials and S3 paramters
AWS_KEY=${AWS_ACCESS_KEY_ID?}
AWS_SECRET=${AWS_SECRET_ACCESS_KEY?}
S3_BUCKET_PATH="/${APP_ENVIRONMENT?}/${APP_NAME?}/${HOSTNAME?}/"
S3_BUCKET_URL=${BUCKET_URL?}

getBucketNavn(){
    if [ ${FASIT_ENVIRONMENT_NAME?} = "p" ]; then
        echo "${APP_NAME?}-prod-fss"
    else
        echo "${APP_NAME?}-preprod-fss"
	fi
}

s3Upload(){
  path=$1
  file=$2

  bucket=$(getBucketNavn)
  echo "bucket navn er ${bucket}"
  bucket_path=${S3_BUCKET_PATH}

  date=$(date -u -R)
  content_type="application/octet-stream"

  sig_string="PUT\n\n$content_type\n$date\n/$bucket$bucket_path$file"

  signature=$(printf "${sig_string}" | openssl sha1 -hmac "${AWS_SECRET}" -binary | base64)

  bucket_hostname=$(printf "${S3_BUCKET_URL}" | awk -F[/:] '{print $4}')

  wget --method=PUT --no-check-certificate --header="Host: ${bucket_hostname}" \
  --header="Date: $date" \
  --header="Content-Type: $content_type" \
  --header="Authorization: AWS ${AWS_KEY}:$signature" \
  --body-file="$path/$file" "${S3_BUCKET_URL}/$bucket$bucket_path$file"

}

# set the path based on the first argument
path="."

# loop through the path and upload the files
for file in "$path"/*; do

   filename="${file##*/}"
   extension="${filename##*.}"
   echo "file name is $filename extension is $extension"
   if [ ${extension} = "hprof" ] || [ ${extension} = "log" ]; then
        s3Upload "$path" "${file##*/}" "/"
   fi
done