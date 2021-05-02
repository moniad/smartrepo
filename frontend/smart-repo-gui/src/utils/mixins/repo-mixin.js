import {mapState} from 'vuex';

const repoMixin ={
    computed: {
        ...mapState('repo', {
            files:(state) => state.files
        })
    }
}
export {repoMixin}