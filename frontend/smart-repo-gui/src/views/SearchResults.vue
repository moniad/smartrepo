<template>
  <v-sheet height="100%" class="overflow-hidden" style="position: relative">
    <v-container :fluid="true" class>
      <v-data-table :headers="headers" :items="items">
        <template #item="{ item }">
          <tr @click="to(item.name, item.directory)">
            <td class="pr-0">
              <v-layout justify-end>
                <v-icon v-if="item.directory">mdi-folder</v-icon>
                <v-icon v-else-if="item.type === 'png'"
                  >mdi-image-outline</v-icon
                >
                <v-icon v-else>mdi-file-outline</v-icon>
              </v-layout>
            </td>
            <td>{{ item.name }}</td>
            <td>{{ parseDate(item.createDate) }}</td>
            <td>{{ item.extension }}</td>
            <td>{{ item.size }}</td>
            <td class="pl-0">
              <v-btn
                class="ma-2"
                icon
                @click.stop="
                  drawerItem = item;
                  drawerActive = !drawerActive;
                "
              >
                <v-icon>mdi-information-outline</v-icon>
              </v-btn>
            </td>
          </tr>
        </template>
      </v-data-table>
    </v-container>
  </v-sheet>
</template>
<script>
import { mapActions } from "vuex";
import { repoMixin } from "../utils/mixins/repo-mixin";
import { dataMixin } from "../utils/mixins/handle-data";

export default {
  name: "results",
  data: () => ({}),
  components: {},
  mixins: [repoMixin, dataMixin],
  computed: {
    headers() {
      return [
        { text: "", value: "icon", align: "end" },
        { text: "Name", value: "name" },
        { text: "Upload Date", value: "createDate" },
        { text: "Type", value: "extension" },
        { text: "Size", value: "size" },
        { text: "Info", value: "info" },
      ];
    },
    items() {
      return this.files;
    },
  },
  methods: {
    ...mapActions("repo", ["search"]),
  },
  beforeMount() {
    this.search({phrase: this.$route.query.phrase, languages: this.$route.query.languages});
  },
};
</script>