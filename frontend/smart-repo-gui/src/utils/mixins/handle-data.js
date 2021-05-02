const dataMixin = {
    methods: {
        parseDate: function(unix_timestamp){
            if(!unix_timestamp) return '-'
            const a = new Date(unix_timestamp);
            const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
            const year = a.getFullYear();
            const month = months[a.getMonth()];
            const date = a.getDate();
            const hour = a.getHours();
            const min = a.getMinutes();
            const sec = a.getSeconds();
            return date + ' ' + month + ' ' + year + ' ' + hour + ':' + min + ':' + sec ;
        }
    }
}
export {dataMixin}