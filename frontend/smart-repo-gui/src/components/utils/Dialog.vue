<template>
  <v-dialog :width="width" v-model="component">
    <template v-slot:activator="{ on }">
      <v-btn :outlined="outlined" v-on="on" :label="label" />
    </template>
    <v-card class="pa-1">
      <v-card-title class="headline text-uppercase">
        {{title }}
        <v-spacer />
        <v-btn color="dark" @button-pressed="hide" icon svg="close" />
      </v-card-title>
      <div class="pa-5">
        <slot name="content" />
      </div>
      <fragment v-if="$scopedSlots['actions']">
        <v-divider />
        <v-card-actions>
          <v-spacer />
          <slot name="actions" :cancel="hide" />
        </v-card-actions>
      </fragment>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  name: "dialog",
  props: {
    outlined: Boolean,
    label: { type: String, default: "Missing Label" },
    title: { type: String, default: "Missing Title" },
    width: { type: String, default: "60vw" },
  },
  data() {
    return { component: false };
  },
  methods: {
    hide() {
      this.component = false;
    },
    show() {
      this.component = true;
    },
    switch() {
      this.component = !this.component;
    },
  },
  computed: {
    isVisible() {
      return this.component;
    },
  },
}
</script>

<style scoped>

</style>