/* eslint-disable no-unused-vars */
import axios from "axios";
const defaultState = () => {
    return {
        files: [],
    };
};

const repoModule = {
    namespaced: true,
    state: () => {
        defaultState()
    },
    mutations: {

    },
    actions: {
        uploadFiles(
            { commit, dispatch, rootGetters, getters, rootState, state }, file
        ) {
            axios.post("http://localhost:7777/upload", file,
                {
                    headers: {
                        'Content-Type': 'multipart/form-data'
                    }
                }
            )
                .then(async response => {
                    if (response.status === 200) {
                        console.log("Response: " + response.data)
                    } else {
                        console.log("ERROR: (" + response.status + ")")
                    }
                })
                .catch(error => {
                    console.error("An error occurred during receiving response!\n", error);
                })
        },
        loadFiles({ commit, dispatch, rootGetters, getters, rootState, state }, name){
            console.log(name)
            let path = ''
            if(name) path = path + name
            console.log(path)
            axios.get("http://localhost:7777/files", {params:{
                path:path
                }})
                .then(async response =>{
                    console.log(response)
                })
                .catch(error =>{
                    console.error("An error occurred during receiving response!\n", error)
                })
        },
        fileDelete({ commit, dispatch, rootGetters, getters, rootState, state }, path){
            console.log(path)
            axios.delete("http://localhost:7777/files", {params:{
                path:path
                }})
                .then(async response =>{
                    console.log(response)
                })
                .catch(error =>{
                    console.error("An error occurred during deleting file!\n", error)
                })
        },
        directoryPost({ commit, dispatch, rootGetters, getters, rootState, state }, path){
            console.log(path)
            axios.post("http://localhost:7777/files", {params:{
                path:path
                }})
                .then(async response =>{
                    console.log(response)
                })
                .catch(error =>{
                    console.error("An error occurred during creating directory!\n", error)
                })
        }
    },
    getters:{

    }
}
export default repoModule;