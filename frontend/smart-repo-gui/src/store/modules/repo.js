/* eslint-disable no-unused-vars */
import axios from "axios";

const repoModule = {
    namespaced: true,
    state: () => {

    },
    mutations: {

    },
    actions: {
        uploadFiles(
            { commit, dispatch, rootGetters, getters, rootState, state }, form
        ) {
            axios.post("http://localhost:7777/upload", form,
                {
                    headers: {
                        'Content-Type': 'multipart/form-data'
                    }
                }
            )
                .then(async response => {
                    if (response.status === 200) {
                        console.log("Status of \"comp1\": " + response.data)
                    } else {
                        console.log("ERROR: Cannot get component status from server! (" + response.status + ")")
                    }
                })
                .catch(error => {
                    console.error("An error occurred during receiving response!\n", error);
                })
        },
    },
    getters:{

    }
}
export default repoModule;