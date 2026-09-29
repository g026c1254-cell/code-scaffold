<template>
  <div class="homeContainer">
    <div class="carousel-margin">
      <div class="category-panel">
        <div v-for="(item,index) in types" :key="index" class="type-item" @click="goCategoryGoods(item.id)">
          <span>{{ displayTypeName(item.name) }}</span>
          <i class="el-icon-arrow-right"></i>
        </div>
      </div>
      <div class="carousel-panel" ref="carouselPanel" @touchstart="handleCarouselTouchStart" @touchend="handleCarouselTouchEnd">
        <el-carousel ref="homeCarousel" :height="carouselHeight" :interval="10000">
          <el-carousel-item v-for="item in carousels" :key="item.id">
            <img :src="getImageUrl(item.cover)" class="carousel-img" @error="handleImageError" @click="handleCarouselClick(item)" style="width: 100%; height: 100%; object-fit: cover; cursor: pointer;">
          </el-carousel-item>
        </el-carousel>
      </div>
    </div>

    <section class="notice-card">
      <div class="section-heading notice-heading">
        <div class="notice-title-wrap">
          <div class="notice-accent"></div>
          <div class="section-title">{{ $t('common.notice') }}</div>
        </div>
        <div v-if="user.id" class="publish-actions">
          <el-button class="notice-secondary-button" size="mini" plain @click="openNoticeDialog">{{ $t('common.publishNotice') }}</el-button>
          <el-button class="notice-primary-button" size="mini" @click="openGoodsDialog">{{ $t('common.publishProduct') }}</el-button>
        </div>
      </div>
      <div v-if="notices.length" class="notice-list">
        <article v-for="(item, index) in notices" :key="item.id || index" class="notice-item">
          <button class="notice-item-header" type="button" @click="toggleNotice(index)">
            <span class="notice-date">{{ formatNoticeDate(item.time) }}</span>
            <span class="notice-category">{{ noticeCategory(item) }}</span>
            <span class="notice-item-title">{{ item.name }}</span>
            <span class="notice-header-stats">
              <span class="header-stat" title="閲覧数"><i class="el-icon-view"></i> {{ item.views || 0 }}</span>
              <span class="header-stat" :class="{ 'is-liked': item.isLiked }" title="いいね"><i class="el-icon-star-on"></i> {{ item.likes || 0 }}</span>
              <span class="header-stat" title="コメント数"><i class="el-icon-chat-round"></i> {{ (commentsMap[item.id] || []).length || 0 }}</span>
            </span>
            <i class="el-icon-arrow-down notice-arrow" :class="{ 'is-open': isNoticeActive(index) }"></i>
          </button>
          <div v-show="isNoticeActive(index)" class="notice-item-body">
            <div class="notice-content" v-html="item.content"></div>

            <!-- 互动操作与发布者信息行 -->
            <div class="notice-action-bar">
              <div class="publisher-tag">{{ $t('common.publisher') }}：{{ item.userName || $t('common.anonymous') }}</div>
              <div class="notice-interactive-stats">
                <span class="stat-badge view-badge" title="閲覧数">
                  <i class="el-icon-view"></i>
                  <span>{{ item.views || 0 }} 閲覧</span>
                </span>
                <button
                  type="button"
                  class="like-btn"
                  :class="{ 'is-liked': item.isLiked }"
                  @click.stop="handleLike(item)"
                  title="いいね"
                >
                  <i :class="item.isLiked ? 'el-icon-star-on' : 'el-icon-star-off'"></i>
                  <span>{{ item.isLiked ? 'いいね済' : 'いいね' }} ({{ item.likes || 0 }})</span>
                </button>
              </div>
            </div>

            <!-- 评论区模块 -->
            <div class="notice-comment-section">
              <div class="comment-section-header">
                <span class="comment-section-title">
                  <i class="el-icon-chat-dot-round"></i> コメント
                  <span class="comment-count-badge">({{ (commentsMap[item.id] || []).length }})</span>
                </span>
              </div>

              <!-- 评论输入框 -->
              <div class="comment-input-box">
                <el-input
                  type="textarea"
                  :rows="2"
                  :placeholder="user && user.id ? 'コメントを入力してください...' : 'コメントを投稿するにはログインしてください'"
                  v-model="commentInputs[item.id]"
                  maxlength="300"
                  show-word-limit
                  :disabled="!user || !user.id"
                ></el-input>
                <div class="comment-submit-row">
                  <el-button
                    type="primary"
                    size="small"
                    class="comment-submit-btn"
                    :disabled="!user || !user.id"
                    @click="submitComment(item.id)"
                  >
                    コメント送信
                  </el-button>
                </div>
              </div>

              <!-- 评论列表 -->
              <div class="comment-list" v-loading="commentsLoading[item.id]">
                <div
                  v-for="comment in (commentsMap[item.id] || [])"
                  :key="comment.id"
                  class="comment-item"
                >
                  <img
                    :src="getImageUrl(comment.userAvatar)"
                    class="comment-avatar"
                    @error="handleImageError"
                  />
                  <div class="comment-content-wrap">
                    <div class="comment-meta">
                      <span class="comment-username">{{ comment.userName }}</span>
                      <span class="comment-time">{{ formatCommentTime(comment.createTime) }}</span>
                      <el-button
                        v-if="user && user.id && (user.id === comment.userId || user.role === 'ADMIN')"
                        type="text"
                        size="mini"
                        class="comment-del-btn"
                        @click="deleteComment(item.id, comment.id)"
                      >
                        削除
                      </el-button>
                    </div>
                    <div class="comment-text">{{ comment.content }}</div>
                  </div>
                </div>
                <div
                  v-if="!commentsLoading[item.id] && (!commentsMap[item.id] || commentsMap[item.id].length === 0)"
                  class="no-comments-tip"
                >
                  まだコメントがありません。最初のコメントを投稿しましょう！
                </div>
              </div>
            </div>
          </div>
        </article>
      </div>
      <el-empty v-else :description="$t('common.noNotice')"></el-empty>
    </section>

    <el-dialog class="mobile-publish-dialog notice-publish-dialog" :title="$t('common.publishNotice')" :visible.sync="noticeDialogVisible" width="460px" :close-on-click-modal="false">
      <el-form ref="noticeForm" :model="noticeForm" :rules="noticeRules" label-width="80px">
        <el-form-item :label="$t('common.title')" prop="name">
          <el-input v-model="noticeForm.name" maxlength="100" show-word-limit :placeholder="$t('common.title')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('common.content')" prop="content">
          <div class="notice-editor">
            <Toolbar :editor="noticeEditor" :defaultConfig="toolbarConfig" mode="default" />
            <Editor
              v-model="noticeForm.content"
              :defaultConfig="editorConfig"
              mode="default"
              @onCreated="onNoticeEditorCreated"
            />
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="noticeDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitNotice">{{ $t('common.publish') }}</el-button>
      </div>
    </el-dialog>

    <el-dialog class="mobile-publish-dialog goods-publish-dialog" :title="$t('common.publishProduct')" :visible.sync="goodsDialogVisible" width="560px" :close-on-click-modal="false">
      <el-form ref="goodsForm" :model="goodsForm" :rules="goodsRules" label-width="90px">
        <el-form-item :label="$t('common.productName')" prop="name">
          <el-input v-model="goodsForm.name" maxlength="100" :placeholder="$t('common.productName')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('common.productCategory')" prop="typeId" class="category-form-item">
          <div class="category-chips-grid">
            <div
              v-for="type in types"
              :key="type.id"
              class="category-chip"
              :class="{ 'is-selected': goodsForm.typeId === type.id }"
              @click="handleSelectCategory(type.id)"
            >
              <i class="el-icon-check check-mark" v-if="goodsForm.typeId === type.id"></i>
              <span class="chip-label">{{ displayTypeName(type.name) }}</span>
            </div>
          </div>
          <el-input v-model="goodsForm.typeId" style="display: none;"></el-input>
        </el-form-item>
        <el-form-item :label="$t('common.price')" prop="price">
          <el-input-number v-model="goodsForm.price" :min="0.01" :precision="2" :step="1" controls-position="right" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item :label="$t('common.stock')" prop="store">
          <el-input-number v-model="goodsForm.store" :min="1" :step="1" controls-position="right" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="商品画像" prop="cover">
          <div class="goods-upload-box">
            <el-upload
                action=""
                :http-request="uploadGoodsCover"
                :show-file-list="false"
                accept="image/*"
                :before-upload="beforeGoodsCoverUpload">
              <div v-if="goodsForm.cover" class="cover-preview-wrapper">
                <img :src="getImageUrl(goodsForm.cover)" class="goods-cover-preview">
                <div class="cover-overlay">
                  <i class="el-icon-camera"></i>
                  <span>画像を変更</span>
                </div>
              </div>
              <div v-else class="upload-placeholder-card">
                <i class="el-icon-upload upload-icon"></i>
                <div class="upload-tip-text">クリックして商品画像をアップロード</div>
                <div class="upload-sub-tip">JPG / PNG / WEBP形式対応</div>
              </div>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="商品説明" prop="descr">
          <el-input
            type="textarea"
            :rows="4"
            v-model="goodsForm.descr"
            maxlength="1000"
            show-word-limit
            placeholder="商品の状態（キズ・汚れの有無）、使用期間、サイズ、付属品などを詳しく記入してください"
          ></el-input>
        </el-form-item>
        <el-form-item label="購入注意事項" prop="purchaseNotice">
          <el-input
            type="textarea"
            :rows="3"
            v-model="goodsForm.purchaseNotice"
            maxlength="500"
            show-word-limit
            placeholder="例：八王子キャンパス内での手渡し希望、平日の夕方対応可能、即購入OK、返品不可など"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="goodsDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitGoods">{{ $t('common.submitProduct') }}</el-button>
      </div>
    </el-dialog>

    <div style="margin-top: 30px">
      <div class="section-heading">
        <div class="section-title section-title-accent">
          <h1>{{ $t('common.newArrivals') }}</h1>
        </div>
        <div>
          <el-link @click="goPage('/front/goods')" :underline="false">{{ $t('common.viewMore') }}</el-link>
        </div>
      </div>
      <div>
        <el-row :gutter="20">
          <el-col :xs="12" :sm="8" :md="6" v-for="(item,index) in timeGoods" :key="index" class="goods-col">
            <el-card :body-style="{ padding: '0px' }" class="card-item" @click.native="goGoodsDetail(item.id)">
              <img :src="getImageUrl(item.cover)" alt="" @error="handleImageError" class="goods-image">
              <div class="goods-content">
                <div class="goods-name">
                  {{item.name}}
                </div>
                <div class="goods-descr">
                  {{ stripHtml(item.content || item.descr) }}
                </div>
                <div class="goods-meta">
                  <div class="goods-price">{{item.price}}円</div>
                </div>
                <div class="publisher-tag">{{ $t('common.publisher') }}：{{ item.userName || $t('common.anonymous') }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>

  </div>
</template>

<script>
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'

export default {
  name: 'Home',
  components: { Editor, Toolbar },
  data() {
    return {
      carousels: [],
      types: [],
      timeGoods: [],
      notices: [],
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      noticeDialogVisible: false,
      goodsDialogVisible: false,
      noticeForm: {
        name: '',
        content: ''
      },
      goodsForm: {
        name: '',
        typeId: null,
        price: 0,
        store: 1,
        cover: '',
        descr: '',
        content: ''
      },
      editor: null,
      noticeEditor: null,
      toolbarConfig: {},
      editorConfig: {
        placeholder: '商品説明を入力',
        MENU_CONF: {
          uploadImage: {
            server: '',
            fieldName: 'file',
            headers: {},
            allowedFileTypes: ['image/*']
          }
        }
      },
      noticeRules: {
        name: [{ required: true, message: 'お知らせタイトルを入力', trigger: 'blur' }],
        content: [{ required: true, message: 'お知らせ内容を入力', trigger: 'blur' }]
      },
      goodsRules: {
        name: [{ required: true, message: '商品名を入力', trigger: 'blur' }],
        typeId: [{ required: true, message: '商品カテゴリを選択', trigger: 'change' }],
        price: [{ required: true, message: '商品価格を入力', trigger: 'change' }],
        cover: [{ required: true, message: '商品画像をアップロード', trigger: 'change' }]
      },
      activeNames: [],
      commentsMap: {},
      commentsLoading: {},
      commentInputs: {},
      carouselHeight: '440px',
      carouselTouchStartX: 0
    }
  },
  created() {
    this.configureEditor()
    this.loadType()
    this.loadCarousel()
    this.loadTimeGoods()
    this.loadNotice()
    this.updateCarouselHeight()
    window.addEventListener('resize', this.updateCarouselHeight)
  },
  beforeDestroy() {
    if (this.editor) this.editor.destroy()
    if (this.noticeEditor) this.noticeEditor.destroy()
    window.removeEventListener('resize', this.updateCarouselHeight)
  },
  methods: {
    configureEditor() {
      this.editorConfig.MENU_CONF.uploadImage.server = this.$baseUrl + '/file/editor/upload'
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      this.editorConfig.MENU_CONF.uploadImage.headers = { token: user.token || '' }
    },
    loadCarousel(){
      this.$request.get('/carousel/selectAll').then(res => {
        this.carousels = Array.isArray(res.data) ? res.data : []
      }).catch(() => {
        this.carousels = []
      })
    },
    loadType(){
      this.$request.get('/type/selectAll').then(res => {
        this.types = Array.isArray(res.data) ? res.data : []
      })
    },
    displayTypeName(name) {
      if (!name) return ''
      const categoryMap = {
        '零食': 'お菓子・食品',
        '饮料': '飲料・ドリンク',
        '数码产品': '家電・スマホ',
        '女装': 'レディース',
        '男装': 'メンズ',
        '家具': 'インテリア・家具',
        '办公用品': '文房具・日用品',
        '图书': '本・教科書',
        '美妆': 'コスメ・美容',
        '食品': 'お菓子・食品',
        '日用品': '文房具・日用品'
      }
      if (categoryMap[name]) return categoryMap[name]
      const key = 'category.' + name
      const translated = this.$t(key)
      return translated === key ? name : translated
    },
    updateCarouselHeight() {
      this.carouselHeight = window.innerWidth <= 520 ? '220px' : (window.innerWidth <= 768 ? '300px' : '440px')
    },
    handleCarouselTouchStart(event) {
      if (event.touches && event.touches.length) {
        this.carouselTouchStartX = event.touches[0].clientX
      }
    },
    handleCarouselTouchEnd(event) {
      if (!event.changedTouches || !event.changedTouches.length) return
      const distance = event.changedTouches[0].clientX - this.carouselTouchStartX
      if (Math.abs(distance) < 40 || !this.$refs.homeCarousel) return
      const current = this.$refs.homeCarousel.activeIndex
      if (distance < 0) {
        this.$refs.homeCarousel.next()
      } else if (current > 0) {
        this.$refs.homeCarousel.prev()
      } else {
        this.$refs.homeCarousel.setActiveItem(this.carousels.length - 1)
      }
    },
    toggleNotice(index) {
      const activeIndex = this.activeNames.indexOf(index)
      if (activeIndex >= 0) {
        this.activeNames.splice(activeIndex, 1)
      } else {
        this.activeNames.push(index)
        const notice = this.notices[index]
        if (notice && notice.id) {
          this.$request.get(`/notice/selectById/${notice.id}`).then(res => {
            if (res.code === '200' && res.data) {
              this.$set(notice, 'views', res.data.views)
              this.$set(notice, 'likes', res.data.likes)
              if (typeof res.data.isLiked === 'boolean') {
                this.$set(notice, 'isLiked', res.data.isLiked)
              }
            }
          })
          this.loadComments(notice.id)
        }
      }
    },
    loadComments(noticeId) {
      this.$set(this.commentsLoading, noticeId, true)
      this.$request.get(`/noticeComment/selectByNoticeId/${noticeId}`).then(res => {
        if (res.code === '200') {
          this.$set(this.commentsMap, noticeId, Array.isArray(res.data) ? res.data : [])
        }
      }).catch(() => {
        this.$set(this.commentsMap, noticeId, [])
      }).finally(() => {
        this.$set(this.commentsLoading, noticeId, false)
      })
    },
    handleLike(item) {
      if (!this.user || !this.user.id) {
        this.$message.warning('ログインが必要です')
        this.$router.push({ path: '/login', query: { redirect: this.$route.fullPath } })
        return
      }
      this.$request.post(`/notice/like/${item.id}`).then(res => {
        if (res.code === '200' && res.data) {
          this.$set(item, 'isLiked', res.data.isLiked)
          this.$set(item, 'likes', res.data.likes)
          this.$message.success(res.data.isLiked ? 'いいねしました' : 'いいねを取り消しました')
        } else {
          this.$message.error(res.msg || '操作に失敗しました')
        }
      }).catch(() => {
        this.$message.error('操作に失敗しました')
      })
    },
    submitComment(noticeId) {
      if (!this.user || !this.user.id) {
        this.$message.warning('ログインが必要です')
        this.$router.push({ path: '/login', query: { redirect: this.$route.fullPath } })
        return
      }
      const content = (this.commentInputs[noticeId] || '').trim()
      if (!content) {
        this.$message.warning('コメントを入力してください')
        return
      }
      this.$request.post('/noticeComment/add', {
        noticeId,
        content
      }).then(res => {
        if (res.code === '200') {
          this.$message.success('コメントを投稿しました')
          this.$set(this.commentInputs, noticeId, '')
          this.loadComments(noticeId)
        } else {
          this.$message.error(res.msg || 'コメントの投稿に失敗しました')
        }
      }).catch(() => {
        this.$message.error('コメントの投稿に失敗しました')
      })
    },
    deleteComment(noticeId, commentId) {
      this.$confirm('コメントを削除してもよろしいですか？', '確認', {
        type: 'warning',
        confirmButtonText: '削除',
        cancelButtonText: 'キャンセル'
      }).then(() => {
        this.$request.delete(`/noticeComment/delete/${commentId}`).then(res => {
          if (res.code === '200') {
            this.$message.success('コメントを削除しました')
            this.loadComments(noticeId)
          } else {
            this.$message.error(res.msg || 'コメントの削除に失敗しました')
          }
        }).catch(() => {
          this.$message.error('コメントの削除に失敗しました')
        })
      }).catch(() => {})
    },
    formatCommentTime(time) {
      if (!time) return ''
      return String(time).replace('T', ' ')
    },
    isNoticeActive(index) {
      return this.activeNames.indexOf(index) >= 0
    },
    formatNoticeDate(time) {
      if (!time) return '--/--'
      return String(time).slice(0, 10).replace(/-/g, '/')
    },
    noticeCategory(item) {
      const text = `${item.name || ''} ${item.content || ''}`
      if (/维护|メンテナンス/i.test(text)) return this.$t('common.maintenance')
      if (/活动|キャンペーン/i.test(text)) return this.$t('common.campaign')
      return this.$t('common.noticeCategory')
    },
    loadTimeGoods(){
      this.$request.get('/goods/times').then(res => {
        this.timeGoods = Array.isArray(res.data) ? res.data : []
      })
    },
    goPage(url){
      const target = String(url || '')
      if (target.indexOf('/front/goodsDetail') === 0) {
        const query = {}
        const queryIndex = target.indexOf('?')
        if (queryIndex >= 0) {
          new URLSearchParams(target.slice(queryIndex + 1)).forEach((value, key) => {
            query[key] = value
          })
        }
        this.$router.push({ path: '/front/goodsDetail', query })
        return
      }
      this.$router.push(target)
    },
    goCategoryGoods(id) {
      if (!id) {
        this.$router.push('/front/goods')
        return
      }
      this.$router.push({
        path: '/front/goods',
        query: { selectedCategoryId: id }
      })
    },
    handleCarouselClick(item) {
      this.goPage('/front/goods')
    },
    goGoodsDetail(id){
      if (!id) {
        this.$message.error('商品情報が見つかりません')
        return
      }
      this.$router.push({ name: 'GoodsDetail', query: { id } })
    },
    loadNotice() {
      this.$request.get('/notice/selectAll').then(res => {
        this.notices = Array.isArray(res.data) ? res.data : []
        this.notices.forEach(notice => {
          if (notice && notice.id) {
            this.loadComments(notice.id)
          }
        })
      }).catch(() => {
        this.notices = []
      })
    },
    openNoticeDialog() {
      this.noticeForm = { name: '', content: '' }
      this.noticeDialogVisible = true
      this.$nextTick(() => this.$refs.noticeForm && this.$refs.noticeForm.clearValidate())
    },
    submitNotice() {
      this.$refs.noticeForm.validate(valid => {
        if (!valid) return
        if (!this.stripHtml(this.noticeForm.content)) {
          this.$message.error(this.$t('common.content'))
          return
        }
        this.$request.post('/notice/add', this.noticeForm).then(res => {
          if (res.code === '200') {
            this.$message.success('お知らせを投稿しました')
            this.noticeDialogVisible = false
            this.loadNotice()
          } else {
            this.$message.error(res.msg || 'お知らせの投稿に失敗しました')
          }
        })
      })
    },
    handleSelectCategory(id) {
      this.goodsForm.typeId = id
      this.$nextTick(() => {
        if (this.$refs.goodsForm) {
          this.$refs.goodsForm.validateField('typeId')
        }
      })
    },
    openGoodsDialog() {
      this.goodsForm = { name: '', typeId: null, price: undefined, store: 1, cover: '', descr: '', purchaseNotice: '', content: '' }
      this.goodsDialogVisible = true
      this.loadType()
      this.$nextTick(() => this.$refs.goodsForm && this.$refs.goodsForm.clearValidate())
    },
    beforeGoodsCoverUpload(file) {
      const isImage = /^image\//.test(file.type)
      if (!isImage) this.$message.error('商品画像を指定してください')
      return isImage
    },
    uploadGoodsCover(options) {
      const formData = new FormData()
      formData.append('file', options.file)
      this.$request.post('/file/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }).then(res => {
        if (res.code === '200') {
          this.goodsForm.cover = res.data
          options.onSuccess(res)
        } else {
          options.onError(new Error(res.msg || '画像のアップロードに失敗しました'))
        }
      }).catch(options.onError)
    },
    submitGoods() {
      this.$refs.goodsForm.validate(valid => {
        if (!valid) return
        let combinedContent = this.goodsForm.descr || ''
        if (this.goodsForm.purchaseNotice) {
          combinedContent += '\n<!--PURCHASE_NOTICE_START-->\n' + this.goodsForm.purchaseNotice
        }
        const form = Object.assign({}, this.goodsForm, {
          descr: this.goodsForm.descr,
          content: combinedContent
        })
        this.$request.post('/goods/add', form).then(res => {
          if (res.code === '200') {
            this.$message.success('商品を出品しました')
            this.goodsDialogVisible = false
            this.loadTimeGoods()
          } else {
            this.$message.error(res.msg || '商品の出品に失敗しました')
          }
        })
      })
    },
    stripHtml(value) {
      if (!value) return ''
      const container = document.createElement('div')
      container.innerHTML = value
      return (container.textContent || container.innerText || '').replace(/\s+/g, ' ').trim().slice(0, 300)
    },
    onEditorCreated(editor) {
      this.editor = Object.seal(editor)
    },
    onNoticeEditorCreated(editor) {
      this.noticeEditor = Object.seal(editor)
    },
    getImageUrl(url) {
      if (!url) return require('@/assets/empty.svg')
      if (/^data:/i.test(url)) return url
      if (/^https?:\/\//i.test(url)) {
        try {
          const imageUrl = new URL(url)
          if (imageUrl.pathname.indexOf('/file/download/') === 0) {
            return this.$baseUrl + imageUrl.pathname + imageUrl.search
          }
        } catch (e) {
          return require('@/assets/empty.svg')
        }
        return url
      }
      return this.$baseUrl + (url.startsWith('/') ? '' : '/') + url
    },
    handleImageError(event) {
      const fallback = require('@/assets/empty.svg')
      if (event.target.src !== fallback) event.target.src = fallback
    }
  }
}
</script>

<style scoped>
.homeContainer{
  width: min(1240px, 94%);
  margin: 0 auto;
  min-height: 90vh;
  padding-bottom: 48px;
}

.carousel-margin{
  margin: 24px 0 32px;
  display: flex;
  gap: 14px;
  align-items: stretch;
}

.category-panel{
  flex: 1.7;
  min-width: 0;
  padding: 12px 0;
  background: #fff;
  border: 1px solid rgba(226, 232, 240, .8);
  border-radius: 16px;
  box-shadow: 0 10px 25px -5px rgba(15, 23, 42, .08), 0 8px 10px -6px rgba(15, 23, 42, .04);
  overflow: hidden;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.carousel-panel{
  flex: 8.3;
  min-width: 0;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 25px -5px rgba(15, 23, 42, .08), 0 8px 10px -6px rgba(15, 23, 42, .04);
}

.carousel-img{
  width: 100%;
}

.type-item{
  margin: 0 10px;
  padding: 0 16px;
  height: 38px;
  line-height: 38px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 8px;
  color: #334155;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color .2s ease, transform .2s ease, color .2s ease;
}

.type-item:hover{
  color: #ff8a3d;
  background: #fff7ed;
  transform: translateX(3px);
}

.type-item i{
  color: #94a3b8;
  font-size: 14px;
  transition: transform .2s ease, color .2s ease;
}

.type-item:hover i{
  color: #ff8a3d;
  transform: translateX(2px);
}

.notice-card,
.card-item {
  background: #fff;
  border-radius: 16px;
  border: 1px solid rgba(226, 232, 240, .8);
  box-shadow: 0 10px 25px -5px rgba(15, 23, 42, .08), 0 8px 10px -6px rgba(15, 23, 42, .04);
  transition: transform .25s ease, box-shadow .25s ease;
}

.card-item:hover{
  cursor: pointer;
  transform: translateY(-5px);
  box-shadow: 0 10px 24px rgba(48, 49, 51, .12);
}

.notice-card {
  margin-top: 20px;
  padding: 24px;
}

.notice-heading {
  margin-top: 0;
  margin-bottom: 16px;
}

.notice-title-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notice-accent {
  width: 4px;
  height: 28px;
  border-radius: 4px;
  background: linear-gradient(180deg, #ff8a3d, #ffa366);
}

.notice-heading .section-title {
  margin-bottom: 0;
}

.publish-actions {
  display: flex;
  gap: 8px;
}

.notice-primary-button,
.notice-secondary-button {
  border-radius: 9px;
  transition: transform .2s ease, box-shadow .2s ease, background .2s ease;
}

.notice-primary-button {
  border: none;
  color: #fff;
  background: linear-gradient(135deg, #ffa86b, #ff7e29);
}

.notice-primary-button:hover,
.notice-primary-button:focus {
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(255, 126, 41, .25);
}

.notice-secondary-button {
  color: #64748b;
  border: 1px dashed #cbd5e1;
  background: #f8fafc;
}

.notice-secondary-button:hover,
.notice-secondary-button:focus {
  color: #ff8a3d;
  border-color: #fed7aa;
  background: #fff7ed;
}

.notice-list {
  border-top: 1px solid #f1f5f9;
}

.notice-item {
  border-bottom: 1px solid #f1f5f9;
}

.notice-item-header {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px 8px;
  border: 0;
  color: #334155;
  background: transparent;
  text-align: left;
  cursor: pointer;
  transition: background-color .2s ease;
}

.notice-item-header:hover {
  background: #f8fafc;
}

.notice-date {
  flex: 0 0 auto;
  padding: 4px 8px;
  border-radius: 6px;
  color: #475569;
  background: #f1f5f9;
  font-size: 12px;
  font-variant-numeric: tabular-nums;
}

.notice-category {
  flex: 0 0 auto;
  padding: 3px 8px;
  border: 1px solid #fed7aa;
  border-radius: 999px;
  color: #ff8a3d;
  font-size: 11px;
}

.notice-item-title {
  flex: 1;
  overflow: hidden;
  font-size: 14px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-arrow {
  color: #94a3b8;
  transition: transform .2s ease;
}

.notice-arrow.is-open {
  transform: rotate(180deg);
}

.notice-header-stats {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #94a3b8;
  margin-right: 4px;
}

.header-stat {
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-stat.is-liked {
  color: #ff8a3d;
}

.notice-item-body {
  padding: 0 16px 20px 8px;
  color: #64748b;
  line-height: 1.7;
}

.notice-action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px dashed #e2e8f0;
}

.notice-interactive-stats {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: #64748b;
  background: #f8fafc;
  padding: 4px 10px;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
}

.like-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
  background: #f8fafc;
  border: 1px solid #cbd5e1;
  padding: 5px 14px;
  border-radius: 20px;
  cursor: pointer;
  transition: all .2s ease;
}

.like-btn:hover {
  color: #ff8a3d;
  border-color: #fdba74;
  background: #fff7ed;
  transform: translateY(-1px);
}

.like-btn.is-liked {
  color: #ea580c;
  background: #ffedd5;
  border-color: #f97316;
  font-weight: 600;
}

.like-btn i {
  font-size: 15px;
}

.notice-comment-section {
  margin-top: 20px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.comment-section-header {
  margin-bottom: 12px;
}

.comment-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  display: flex;
  align-items: center;
  gap: 6px;
}

.comment-count-badge {
  color: #ff8a3d;
}

.comment-input-box {
  margin-bottom: 16px;
}

.comment-submit-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.comment-submit-btn {
  background: linear-gradient(135deg, #ffa86b, #ff7e29);
  border: none;
  border-radius: 6px;
  font-weight: 500;
}

.comment-submit-btn:hover {
  background: linear-gradient(135deg, #ff9b57, #ff7014);
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 10px 12px;
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid #f1f5f9;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
  border: 1px solid #e2e8f0;
}

.comment-content-wrap {
  flex: 1;
  min-width: 0;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.comment-username {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
}

.comment-time {
  font-size: 11px;
  color: #94a3b8;
}

.comment-del-btn {
  margin-left: auto;
  color: #ef4444;
  padding: 0;
}

.comment-del-btn:hover {
  color: #dc2626;
}

.comment-text {
  font-size: 13px;
  color: #475569;
  line-height: 1.5;
  word-break: break-word;
  white-space: pre-wrap;
}

.no-comments-tip {
  text-align: center;
  font-size: 13px;
  color: #94a3b8;
  padding: 16px 0;
}

.publisher-tag {
  display: inline-block;
  margin-top: 12px;
  padding: 3px 9px;
  border-radius: 12px;
  color: #606266;
  background: #f4f4f5;
  font-size: 12px;
}

.goods-cover-uploader {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 110px;
  height: 110px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  color: #8c939d;
  font-size: 28px;
}

.goods-editor {
  overflow: hidden;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.goods-editor >>> .w-e-text-container {
  min-height: 150px;
}

.notice-editor {
  overflow: hidden;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.notice-editor >>> .w-e-text-container {
  min-height: 180px;
}

.notice-content {
  overflow-wrap: anywhere;
}

.notice-content >>> img,
.notice-content img {
  max-width: 100%;
  height: auto;
}

.goods-cover-preview {
  display: block;
  width: 110px;
  height: 110px;
  border-radius: 6px;
  object-fit: cover;
}

.section-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 30px;
}

.section-title {
  color: #303133;
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 15px;
}

.section-title-accent {
  border-left: 5px solid #ff8a3d;
  padding-left: 10px;
  margin-bottom: 0;
}

.goods-col {
  margin-top: 18px;
}

.goods-image {
  display: block;
  width: 100%;
  height: 200px;
  object-fit: contain !important;
  background: #ffffff;
}

.goods-content {
  padding: 12px;
}

.goods-name {
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.goods-descr {
  margin-top: 7px;
  color: #909399;
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.goods-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.goods-price {
  color: #ff7e29;
  font-size: 20px;
  font-weight: 700;
}



/* 1. 确保包裹两个组件的 Flex 容器占满宽度，并且没有内边距导致它们无法靠边 */
.menu-and-carousel-container {
  display: flex;
  justify-content: space-between; /* 或者使用其他布局，确保右侧靠边 */
  padding: 0; /* 核心修改：去掉任何可能导致无法靠边的内边距 */
  margin-top: 0; /* 核心修改：去掉顶部 margin，让它和导航栏对齐 */
  width: 100%; /* 确保容器撑满屏幕宽度，除非布局有特殊要求 */
}

/* 2. 确保左侧菜单高度占满它所在的侧边栏空间 */
.left-menu {
  flex: 0 0 calc(20%); /* 或者固定宽度 */
  height: 100%; /* 如果希望左侧占满父容器高度 */
  display: flex;
  flex-direction: column;
}

/* 3. 确保轮播图组件所在的容器也占满剩余空间并右对齐 */
.carousel-wrapper {
  flex: 1; /* 核心修改：占据剩余所有空间 */
  display: flex;
  justify-content: flex-end; /* 确保右对齐 */
  overflow: hidden; /* 防止内部内容撑破布局 */
}

@media (max-width: 768px) {
  .homeContainer {
    width: 94%;
  }

  .carousel-margin {
    flex-direction: column;
    gap: 10px;
  }

  .category-panel {
    order: 2;
    padding: 8px 4px;
    display: block;
  }

  .carousel-panel {
    order: 1;
    width: 100%;
  }

  .type-item {
    display: inline-flex;
    width: calc(50% - 24px);
    margin: 3px 8px;
    height: 34px;
    line-height: 34px;
    box-sizing: border-box;
    padding: 0 10px;
    font-size: 13px;
  }

  .notice-card {
    padding: 18px 14px;
  }

  .notice-heading {
    align-items: flex-start;
    flex-wrap: wrap;
    gap: 12px;
  }

  .notice-item-header {
    flex-wrap: wrap;
    gap: 8px;
  }

  .notice-header-stats {
    width: 100%;
    margin-left: 0;
    justify-content: flex-end;
    font-size: 12px;
  }

  .notice-action-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .notice-interactive-stats {
    width: 100%;
    justify-content: flex-end;
  }

  .publish-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .notice-item-header {
    gap: 7px;
    padding-left: 2px;
    padding-right: 2px;
  }

  .notice-category {
    display: none;
  }

  .notice-item-body {
    padding-left: 2px;
    padding-right: 2px;
  }

  .notice-item-title {
    white-space: normal;
    line-height: 1.5;
  }

  .goods-image {
    height: 150px;
  }

  .goods-content {
    padding: 10px;
  }

  .goods-price {
    font-size: 17px;
  }
}

.category-chips-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  width: 100%;
}

.category-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 14px;
  background: transparent !important;
  background-color: transparent !important;
  border: 1px solid #fed7aa;
  border-radius: 18px;
  font-size: 13px;
  color: #475569;
  cursor: pointer;
  box-shadow: none !important;
  transition: all .2s cubic-bezier(0.4, 0, 0.2, 1);
  user-select: none;
  -webkit-tap-highlight-color: transparent;
}

.category-chip:hover {
  border-color: #ff8a3d;
  color: #ff8a3d;
  background: transparent !important;
  background-color: transparent !important;
}

.category-chip.is-selected {
  background: transparent !important;
  background-color: transparent !important;
  border: 2px solid #ff8a3d !important;
  color: #ea6b1f;
  font-weight: 700;
  box-shadow: none !important;
}

.category-chip .check-mark {
  font-size: 12px;
  font-weight: bold;
  color: #ff8a3d;
}

@media (max-width: 520px) {
  .category-chips-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }

  .category-chip {
    justify-content: center;
    padding: 10px 6px;
    font-size: 13px;
    border-radius: 10px;
    text-align: center;
  }

  .notice-heading,
  .section-heading {
    flex-direction: column;
    align-items: stretch;
  }

  .publish-actions {
    justify-content: stretch;
  }

  .publish-actions .el-button {
    flex: 1;
    margin-left: 0;
  }

  .section-title {
    font-size: 18px;
  }

  .mobile-publish-dialog {
    width: calc(100% - 24px) !important;
    max-width: 520px;
    margin: 10vh auto 0 !important;
  }

  .mobile-publish-dialog >>> .el-dialog {
    display: flex;
    flex-direction: column;
    max-height: 82vh;
    overflow: hidden;
    border-radius: 14px;
  }

  .mobile-publish-dialog >>> .el-dialog__header {
    flex: 0 0 auto;
    padding: 18px 18px 12px;
  }

  .mobile-publish-dialog >>> .el-dialog__body {
    flex: 1 1 auto;
    overflow-x: hidden;
    overflow-y: auto;
    padding: 12px 16px;
    -webkit-overflow-scrolling: touch;
  }

  .mobile-publish-dialog >>> .el-dialog__footer {
    flex: 0 0 auto;
    padding: 10px 16px 16px;
  }

  .mobile-publish-dialog >>> .el-form-item {
    margin-bottom: 16px;
  }

  .mobile-publish-dialog >>> .el-form-item__label {
    float: none;
    display: block;
    width: 100% !important;
    padding: 0 0 5px;
    line-height: 1.4;
    text-align: left;
  }

  .mobile-publish-dialog >>> .el-form-item__content {
    margin-left: 0 !important;
    line-height: normal;
  }

  .mobile-publish-dialog >>> .el-dialog__footer .el-button {
    min-width: 96px;
    margin: 0 0 0 6px;
  }

  .notice-publish-dialog >>> .notice-editor,
  .goods-publish-dialog >>> .goods-editor {
    max-width: 100%;
  }

  .mobile-publish-dialog >>> .w-e-toolbar {
    flex-wrap: wrap;
    overflow: hidden;
  }

  .mobile-publish-dialog >>> .w-e-text-container {
    min-height: 140px;
    max-height: 32vh;
    overflow-y: auto;
  }

  .goods-publish-dialog >>> .el-input-number {
    width: 100% !important;
  }

  .goods-publish-dialog >>> .el-upload {
    max-width: 100%;
  }
}

.goods-upload-box {
  width: 100%;
}
.cover-preview-wrapper {
  position: relative;
  width: 130px;
  height: 130px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #fed7aa;
  cursor: pointer;
}
.cover-preview-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cover-overlay {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.65);
  color: #fff;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 4px 0;
}
.upload-placeholder-card {
  width: 100%;
  max-width: 320px;
  height: 110px;
  border: 2px dashed #fed7aa;
  border-radius: 12px;
  background: #fff7ed;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all .2s ease;
}
.upload-placeholder-card:hover {
  border-color: #ff8a3d;
  background: #ffedd5;
}
.upload-icon {
  font-size: 28px;
  color: #ff8a3d;
  margin-bottom: 4px;
}
.upload-tip-text {
  font-size: 13px;
  font-weight: 600;
  color: #ea6b1f;
}
.upload-sub-tip {
  font-size: 11px;
  color: #9a3412;
  margin-top: 2px;
}

</style>