# cursive-path
A program in Kotlin Multiplatform to translate from english to a printed form of language and its cursive form


## Test requests:

```shell
curl -X POST "http://localhost:5000/translate" \
  -H "Content-Type: application/json" \
  -d '{
    "q": "cat",
    "source": "en",
    "target": "pt",
    "format": "text"
}'
```

```shell
curl -v http://localhost:5000/languages
```

```shell
docker run --rm -it -p 5000:5000 libretranslate/libretranslate
```