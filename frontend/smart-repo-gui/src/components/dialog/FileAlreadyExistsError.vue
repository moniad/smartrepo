<template>
  <v-dialog
      v-model="active"
      width="50%"
      hide-overlay
  >
    <v-card>
      <v-card-title class="headline">
        {{title}}
        <v-spacer />
        <v-icon color="dark" @click.stop="close">mdi-close</v-icon>
      </v-card-title>
      <v-card-text>
        {{description}}
      </v-card-text>
      <v-divider />
      <v-card-actions>
        <v-spacer />
        <v-btn @click.stop="close" >Close</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<script>
/* eslint-disable no-unused-vars */

export default {
  name: "FileAlreadyExistsError",
  data() {
    return {
      title: 'Error'
    };
  },
  components: {
  },
  methods: {
    close() {
      this.active = false
    }
  },
  props: {
    value: Boolean,
    description: {
      type: String,
      default: 'Error: File already exists in this directory.'
    }
  },
  computed: {
    active: {
      get () {
        return this.value
      },
      set (value) {
        this.$emit('input', value)
      }
    }
  }
}
</script>

<style scoped>

</style>