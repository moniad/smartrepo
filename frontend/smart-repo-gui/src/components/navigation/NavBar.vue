<template>
  <v-app-bar app class="black">

    <h3 class="white--text pl-6" @click="to()" :style="{ cursor: 'pointer'}">Smart Repo</h3>
    <v-spacer />

    <upload-file-repo v-if="validContextUploadButton"/>
    <search v-if="validContextSearchButton"/>
  </v-app-bar>
</template>

<script>
import UploadFileRepo from "../dialog/UploadFileRepo";
import Search from "../dialog/Search";

export default {
  name: "nav-bar",
  components: { UploadFileRepo, Search },
  methods:{
    to(){
      this.$router.push('/')
    }
  },
  computed: {
    validContextSearchButton() {
      return this.$route.name !== 'Search'
    },
    validContextUploadButton() {
      return this.$route.name === 'Home'
    }
  }
};
</script>