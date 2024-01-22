import {
  BodyParam,
  Delete,
  Get,
  JsonController,
  Param,
  Post,
  Put,
  QueryParam,
  QueryParams,
} from 'routing-controllers'
import { Result } from '@server/utils/response'
import { PageParams } from '@server/utils/request'

import RssService from '@server/anime/service/RssService'

@JsonController('/anime/rss/subscription')
export class RssController {
  private rssService: RssService = new RssService()

  /**
   * 查询 rss 订阅
   * @param page
   * @param size
   */
  @Get('/')
  async getRssSubscribeList(
    @QueryParam('page') page: number = 0,
    @QueryParam('size') size: number = 10,
  ) {
    return Result.ok(await this.rssService.getRssSubscribeList(new PageParams(page, size)))
  }

  /**
   * 新增 rss 订阅
   * @param link
   */
  @Post('/')
  async createRssSubscribe(@BodyParam('link', { required: true }) link: string) {
    return Result.ok(await this.rssService.createRssSubscribe(link))
  }

  /**
   * 修改 rss 订阅
   * @param id
   * @param title
   * @param ttl
   */
  @Put('/:id')
  async updateRssSubscribe(
    @Param('id') id: number,
    @BodyParam('title') title: string,
    @BodyParam('ttl') ttl: number,
  ) {
    return Result.ok(await this.rssService.updateRssSubscribe(id, title, ttl))
  }

  /**
   * 删除 rss 订阅
   * @param id
   */
  @Delete('/:id')
  async deleteRssSubscribe(@Param('id') id: number) {
    return Result.ok(await this.rssService.deleteRssSubscribe(id))
  }

  /**
   * 标记订阅为已读
   * @param id
   */
  @Put('/:id/read')
  async markRssSubscribeAsRead(@Param('id') id: number) {
    return Result.ok(await this.rssService.markRssSubscribeAsRead(id))
  }

  /**
   * 获取订阅内容
   * @param rssSubscriptionId
   * @param page
   * @param size
   */
  @Get('/:rssSubscriptionId/item')
  async getRssSubscribeItemList(
    @Param('rssSubscriptionId') rssSubscriptionId: number,
    @QueryParam('page') page: number = 0,
    @QueryParam('size') size: number = 10,
  ) {
    return Result.ok(
      await this.rssService.getRssSubscribeItemList(rssSubscriptionId, new PageParams(page, size)),
    )
  }

  /**
   * 获取订阅内容更新
   */
  @Post('/:rssSubscriptionId/item')
  async fetchRssSubscribeItem(rssSubscriptionId: number) {
    return Result.ok(await this.rssService.fetchRssSubscribeItem(rssSubscriptionId))
  }

  /**
   * 将订阅内容标记为已读
   */
  @Put('/:rssSubscriptionId/item/:uuid/read')
  markRssSubscribeItemAsRead(rssSubscriptionId: number, uuid: string) {
    return Result.ok(this.rssService.markRssSubscribeItemAsRead(rssSubscriptionId, uuid))
  }
}
