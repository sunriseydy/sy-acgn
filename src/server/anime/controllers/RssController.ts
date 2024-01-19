import { Get, JsonController } from 'routing-controllers'

@JsonController('/rss')
export class RssController {
  @Get()
  get() {
    return {
      message: '1111',
    }
  }
}
