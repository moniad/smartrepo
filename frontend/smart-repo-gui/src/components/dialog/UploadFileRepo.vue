<template>
  <div>
    <dialog
        v-if="active"
        :outlined="true"
        title="Upload Service Data"
    >
      <template slot="content">
        <file-uploader
            @file-upload="(file) => loadFile(file)"
            file-types=".xlsx,.csv"
        />
      </template>
      <template slot="actions">
        <v-spacer />
        <v-btn plain label="Cancel" @button-pressed="cancel" />
        <v-btn
            @button-pressed="
            onUpload();
            active = false;
          "
            label="Confirm"
        />
      </template>
    </dialog>
  </div>
</template>

<script>
import FileUploader from "@/components/utils/FileUploader";
// import Dialog from "@/components/utils/Dialog";

export default {
  name: "UploadFileRepo",
  data() {
    return { file: null, active: true };
  },
  components: {
     FileUploader
  },
  methods: {
    loadFile(file) {
      console.log(file)
    },
    onUpload() {
    },
    cancel() {
    }
  },
}
</script>

<style scoped>

</style>