package main

import (
	"fmt"
    "log"
    "net/http"
    "encoding/json"
	"github.com/abadojack/whatlanggo"
)

type LanguageDetectorPack struct {
    Phrase string
}

func languageDetectorService(w http.ResponseWriter, r *http.Request) {
    var pack LanguageDetectorPack
    err := json.NewDecoder(r.Body).Decode(&pack)
    if err != nil {
        http.Error(w, err.Error(), http.StatusBadRequest)
        return
    }
	langInfo := whatlanggo.Detect(pack.Phrase)
	w.Header().Set("Content-Type", "application/json")
    fmt.Fprintf(w, "{\"phrase\": \"%+v\",\"language\": \"%+v\"}", pack.Phrase, langInfo.Lang.String())
}

func handleRequests() {
    http.HandleFunc("/langdetect", languageDetectorService)
    fmt.Println("Language Detector Service started.")
    log.Fatal(http.ListenAndServe(":10000", nil))
}

func main() {
    handleRequests()
}