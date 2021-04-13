<template>
  <v-app>
    <nav-bar/>
    <v-main>
      <home/>
    </v-main>
  </v-app>
</template>

<script>
import NavBar from "@/components/navigation/NavBar";
import Home from "@/views/Home";

export default {
  name: 'App',

  components: {
    NavBar,Home
  },

  data: () => ({
    //
  }),
};
</script>
<style lang="scss">
@import "./styles/app.scss";
</style>

