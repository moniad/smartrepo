<template>
  <v-sheet height="100%" class="overflow-hidden" style="position: relative">
    <template>
      <v-form>
        <v-container>
          <v-row>
            <h2 class="mt-6">Search</h2>
          </v-row>
          <v-row>
            <v-text-field
              v-model="phrase"
              label="Phrase"
              required
            ></v-text-field>
          </v-row>
          <v-row> Target language </v-row>
          <v-row class="mt-0 mb-n10">
            <v-col cols="12" md="4">
              <v-checkbox
                value="en"
                id="en"
                v-model="languages"
                :label="`English`"
              ></v-checkbox>
            </v-col>
            <v-col cols="12" md="4">
              <v-checkbox
                value="pl"
                id="pl"
                v-model="languages"
                :label="`Polish`"
              ></v-checkbox>
            </v-col>
            <v-col cols="12" md="4">
              <v-checkbox
                value="es"
                id="es"
                v-model="languages"
                :label="`Spanish`"
              ></v-checkbox>
            </v-col>
          </v-row>
          <v-row class="mt-0">
            <v-col cols="12" md="4">
              <v-checkbox
                value="de"
                id="de"
                v-model="languages"
                :label="`German`"
              ></v-checkbox>
            </v-col>
            <v-col cols="12" md="4">
              <v-checkbox
                value="fr"
                id="fr"
                v-model="languages"
                :label="`French`"
              ></v-checkbox>
            </v-col>
            <v-col cols="12" md="4">
              <v-checkbox
                value="it"
                id="it"
                v-model="languages"
                :label="`Italian`"
              ></v-checkbox>
            </v-col>
          </v-row>
          <v-row>
            <v-col cols="12" md="12" class="d-flex justify-end align-end">
              <v-btn class="mr-5" color="primary" @click="to()">
                Search
              </v-btn>
              <v-btn to="/">Cancel</v-btn>
            </v-col>
          </v-row>
        </v-container>
      </v-form>
    </template>
  </v-sheet>
</template>

<script>
export default {
  name: "search",
  data: () => ({
    phrase: "",
    languages: ["en"],
  }),
  methods: {
    to() {
      this.$router.push({
          path: '/results',
          query: { phrase: this.phrase, languages: this.languages }
        });
    },
  },
};
</script>
