import { createVuetify } from 'vuetify'
import { aliases, mdi } from 'vuetify/iconsets/mdi'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import { md3 } from 'vuetify/blueprints'
import {zhHans} from "vuetify/locale";

export default createVuetify({
    components,
    directives,
    blueprint: md3,
    locale: {
        locale: 'zhHans',
        messages: { zhHans },
    },
    theme: {
        dark: true,
    },
    icons: {
        defaultSet: 'mdi',
        aliases,
        sets: {
            mdi,
        },
    },
})