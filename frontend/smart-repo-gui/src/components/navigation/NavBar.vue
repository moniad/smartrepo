<template>
  <v-app-bar app class="black">

    <h3 class="white--text pl-6">Smart Repo</h3>
    <v-spacer />

<!--    <search/>-->
    <upload-file-repo/>
  </v-app-bar>
</template>

<script>
import UploadFileRepo from "../dialog/UploadFileRepo";

export default {
  name: "nav-bar",
  components: { UploadFileRepo},
};
</script>