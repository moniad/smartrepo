<template>
  <v-dialog
      v-model="active"
      hide-overlay
  >
    <template v-slot:activator="{ on, attrs }">
      <v-btn
          color="primary"
          dark
          v-bind="attrs"
          v-on="on"
      >
        Upload File
      </v-btn>
    </template>
    <v-card>
      <v-card-title class="headline">
        {{title}}
        <v-spacer />
        <v-icon color="dark" @click="cancel">mdi-close</v-icon>
      </v-card-title>
      <v-card-text>
        <v-container class="dropbox" @dragover="dragover" @dragleave="dragleave" @drop="drop">
          <label for="input">
            <v-row row class="mt-1">
              <v-col cols="4"/>
              <v-col cols="4">
                <v-row class="ml-1">
                  Drop files in here or click to upload files
                </v-row>
                <v-row>
                  <input type="file" id="input" multiple @change="onChange" ref="file" accept="*" style="display: none"/>
                </v-row>
                <v-row class="mb-1" >
                  <ul class="mt-4" v-if="this.fileList.length" v-cloak>
                    <li class="text-sm p-1" v-for="(file,index) in fileList" :key="index">
                      {{file.name}}
                      <button type="button" @click="remove(fileList.indexOf(file))" title="Remove file"> <v-icon>mdi-delete</v-icon></button>
                    </li>
                  </ul>
                </v-row>
              </v-col>
              <v-col cols="4"/>
            </v-row>
          </label>
        </v-container>
      </v-card-text>
      <v-divider />
      <v-card-actions>
        <v-spacer />
        <v-btn plain @click="cancel" >Cancel</v-btn>
        <v-btn
            @click="
            submitFiles();
          "
        >Submit</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<script>
/* eslint-disable no-unused-vars */

import { mapActions } from "vuex";
export default {
  name: "UploadFileRepo",
  data() {
    return {
      active: false,
      fileList: [],
      title: 'Upload File',
    };
  },
  components: {
  },
  methods: {
    ...mapActions("repo",["uploadFiles"]),
    cancel() {
      this.active = !this.active
    },
    submitFiles(){
      let formData = new FormData();
      // for(let i = 0; i < this.fileList.length; i++ ){
      //   let file = this.fileList[i];
      //   formData.append('files[' + i + ']', file);
      // }
      formData.append('files',this.fileList[0])
      console.log(formData.get('files'))
      this.uploadFiles(formData)
    },
    onChange() {
      this.fileList.push(...this.$refs.file.files)
      console.log(this.fileList)
    },
    remove(i) {
      this.fileList.splice(i, 1);
    },
    dragover(event) {
      event.preventDefault();
    },
    dragleave(event) {
      // Clean up
    },
    drop(event) {
      event.preventDefault();
      this.fileList.push(...event.dataTransfer.files)
    }
  },
}
</script>

<style scoped>

</style>