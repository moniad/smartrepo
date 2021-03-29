<template>
  <v-app-bar app class="black">

    <h3 class="white--text pl-6">Smart Repo</h3>
    <v-spacer />

    <search/>
    <upload-file-repo/>
    <v-btn/>
  </v-app-bar>
</template>

<script>
import Search from "@/components/dialog/Search";
import UploadFileRepo from "@/components/dialog/UploadFileRepo";

export default {
  name: "nav-bar",
  data: () => ({
  }),
  components: {Search, UploadFileRepo},
};
</script>