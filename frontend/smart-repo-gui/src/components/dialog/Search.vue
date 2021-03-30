<template>
  <div>
    <v-dialog title="Search" label="search">
      <template #content>
        <v-form v-model="valid">
          <v-container>
            <v-row>
              <v-col
                  cols="12"
                  md="4"
              >
                <v-text-field
                    v-model="firstname"
                    label="Title"
                    required
                ></v-text-field>
              </v-col>

              <v-col
                  cols="12"
                  md="4"
              >
                <v-text-field
                    v-model="lastname"
                    label="Extension"
                    required
                ></v-text-field>
              </v-col>

              <v-col
                  cols="12"
                  md="4"
              >
                <v-text-field
                    v-model="email"
                    label="Author"
                    required
                ></v-text-field>
              </v-col>
            </v-row>
          </v-container>
        </v-form>
      </template>
      <template #actions="{cancel}">
        <v-spacer />
        <v-btn text label="Cancel" @button-pressed="cancel" />
        <v-btn
            @button-pressed="cancel()"
            label="Confirm"
        />
      </template>
    </v-dialog>
  </div>
</template>

<script>
export default {
  name: "Search",
  data:() => ({
    data: () => ({
      valid: false,
      firstname: '',
      lastname: '',
      email: '',
    }),
  })
}
</script>

<style scoped>

</style>