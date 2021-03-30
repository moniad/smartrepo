<template>
<!--  <VueFileAgent-->
<!--      v-model="fileRecords"-->
<!--      style="width:100%;"-->
<!--      v-bind="$props"-->
<!--      :accept="fileTypes"-->
<!--      :errorText="{-->
<!--      type: 'Invalid file type. Only ' + fileTypes + ' Allowed',-->
<!--    }"-->
<!--  />-->
  <v-card>aaaa</v-card>
</template>

<script>
export default {
  name: "FileUploader",
  data() {
    return {
      fileRecords: [],
    };
  },
  props: {
    fileTypes: { type: String },
  },
  watch: {
    fileRecords(input) {
      const file = input.length > 0 ? input[0].file : null;
      this.$emit("file-upload", file);
    },
  },
}
</script>

<style scoped>

</style>