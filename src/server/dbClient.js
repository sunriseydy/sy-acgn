import {PrismaClient} from "@prisma/client";

const prisma = new PrismaClient({
    log: [{level: 'query', emit: 'event'},{level: 'info', emit: 'stdout'},{level: 'warn', emit: 'stdout'},{level: 'error', emit: 'stdout'}]
})
prisma.$on('query', (e) => {
    console.info('prisma:query')
    console.info(e)
})
export default prisma
