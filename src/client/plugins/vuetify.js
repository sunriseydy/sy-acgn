import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import { aliases, mdi } from 'vuetify/iconsets/mdi-svg'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import { md2 } from 'vuetify/blueprints'
import { zhHans } from 'vuetify/locale'

export default createVuetify({
  components,
  directives,
  theme: {
    defaultTheme: 'dark',
  },
  blueprint: md2,
  locale: {
    locale: 'zhHans',
    messages: { zhHans },
  },
  icons: {
    defaultSet: 'mdi',
    aliases,
    sets: {
      mdi,
    },
  },
})
