<template>
  <div>
    <v-btn to="/search" color="primary" dark v-bind="attrs" v-on="on">
      Advanced Search
    </v-btn>
  </div>
</template>

<script>
export default {
  name: "Search",
  data: () => ({
  }),
};
</script>

<style scoped>
</style>