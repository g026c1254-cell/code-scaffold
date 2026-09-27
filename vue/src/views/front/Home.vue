<template>
  <div class="homeContainer">
    <div class="carousel-margin">
      <div class="category-panel">
        <div v-for="(item,index) in types" :key="index" class="type-item" @click="goPage('/front/goods')">
          <span>{{ displayTypeName(item.name) }}</span>
          <i class="el-icon-arrow-right"></i>
        </div>
      </div>
      <div class="carousel-panel" ref="carouselPanel" @touchstart="handleCarouselTouchStart" @touchend="handleCarouselTouchEnd">
        <el-carousel ref="homeCarousel" :height="carouselHeight" :interval="10000">
          <el-carousel-item v-for="item in carousels" :key="item.id">
            <img :src="getImageUrl(item.cover)" class="carousel-img" @error="handleImageError" @click="goPage('/front/goods')" style="width: 100%; height: 100%; object-fit: cover; cursor: pointer;">
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
            <i class="el-icon-arrow-down notice-arrow" :class="{ 'is-open': isNoticeActive(index) }"></i>
          </button>
          <div v-show="isNoticeActive(index)" class="notice-item-body">
            <div class="notice-content" v-html="item.content"></div>
            <div class="publisher-tag">{{ $t('common.publisher') }}：{{ item.userName || $t('common.anonymous') }}</div>
          </div>
        </article>
      </div>
      <el-empty v-else :description="$t('common.noNotice')"></el-empty>
    </section>

    <el-dialog :title="$t('common.publishNotice')" :visible.sync="noticeDialogVisible" width="460px" :close-on-click-modal="false">
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

    <el-dialog :title="$t('common.publishProduct')" :visible.sync="goodsDialogVisible" width="560px" :close-on-click-modal="false">
      <el-form ref="goodsForm" :model="goodsForm" :rules="goodsRules" label-width="90px">
        <el-form-item :label="$t('common.productName')" prop="name">
          <el-input v-model="goodsForm.name" maxlength="100" :placeholder="$t('common.productName')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('common.productCategory')" prop="typeId">
          <el-select v-model="goodsForm.typeId" :placeholder="$t('common.productCategory')" style="width: 100%">
            <el-option v-for="type in types" :key="type.id" :label="displayTypeName(type.name)" :value="type.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('common.price')" prop="price">
          <el-input-number v-model="goodsForm.price" :min="0.01" :precision="2" :step="1" controls-position="right" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item :label="$t('common.stock')" prop="store">
          <el-input-number v-model="goodsForm.store" :min="1" :step="1" controls-position="right" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item :label="$t('common.productImage')" prop="cover">
          <el-upload
              action=""
              :http-request="uploadGoodsCover"
              :show-file-list="false"
              :before-upload="beforeGoodsCoverUpload">
            <img v-if="goodsForm.cover" :src="getImageUrl(goodsForm.cover)" class="goods-cover-preview">
            <i v-else class="el-icon-plus goods-cover-uploader"></i>
          </el-upload>
        </el-form-item>
        <el-form-item :label="$t('common.description')" prop="descr">
          <div class="goods-editor">
            <Toolbar :editor="editor" :defaultConfig="toolbarConfig" mode="default" />
            <Editor
              v-model="goodsForm.content"
              :defaultConfig="editorConfig"
              mode="default"
              @onCreated="onEditorCreated"
            />
          </div>
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
            headers: {}
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
      activeNames: [0],
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
      const categoryMap = {
        '零食': 'お菓子・食品',
        '饮料': '飲料・ドリンク',
        '数码产品': '家電・スマホ',
        '女装': 'レディース',
        '男装': 'メンズ',
        '家具': 'インテリア・家具',
        '办公用品': '文房具・日用品',
        '图书': '本・教科書',
        '美妆': 'コスメ・美容'
      }
      return categoryMap[name] || name
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
      }
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
    openGoodsDialog() {
      this.goodsForm = { name: '', typeId: null, price: 0, store: 1, cover: '', descr: '', content: '' }
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
        const form = Object.assign({}, this.goodsForm, {
          descr: this.stripHtml(this.goodsForm.content) || this.goodsForm.descr
        })
        this.$request.post('/goods/add', form).then(res => {
          if (res.code === '200') {
            this.$message.success('商品を出品しました')
            this.goodsDialogVisible = false
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
  padding: 10px 0;
  background: #fff;
  border: 1px solid rgba(226, 232, 240, .8);
  border-radius: 16px;
  box-shadow: 0 10px 25px -5px rgba(15, 23, 42, .08), 0 8px 10px -6px rgba(15, 23, 42, .04);
  overflow: hidden;
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
  margin: 3px 10px;
  padding: 0 16px;
  height: 34px;
  line-height: 34px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 7px;
  color: #334155;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color .2s ease, transform .2s ease, color .2s ease;
}

.type-item:hover{
  color: #ea580c;
  background: #fff7ed;
  transform: translateX(3px);
}

.type-item i{
  color: #94a3b8;
  font-size: 14px;
  transition: transform .2s ease, color .2s ease;
}

.type-item:hover i{
  color: #ea580c;
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
  background: linear-gradient(180deg, #ff5500, #f59e0b);
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
  background: linear-gradient(90deg, #f97316, #f59e0b);
}

.notice-primary-button:hover,
.notice-primary-button:focus {
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(245, 158, 11, .25);
}

.notice-secondary-button {
  color: #64748b;
  border: 1px dashed #cbd5e1;
  background: #f8fafc;
}

.notice-secondary-button:hover,
.notice-secondary-button:focus {
  color: #ea580c;
  border-color: #fdba74;
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
  color: #c2410c;
  background: #fff7ed;
  font-size: 12px;
  font-variant-numeric: tabular-nums;
}

.notice-category {
  flex: 0 0 auto;
  padding: 3px 8px;
  border: 1px solid #fed7aa;
  border-radius: 999px;
  color: #ea580c;
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

.notice-item-body {
  padding: 0 40px 16px 8px;
  color: #64748b;
  line-height: 1.7;
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
  border-left: 5px solid #ff6700;
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
  object-fit: cover;
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
  color: #ff6700;
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
  }

  .carousel-panel {
    order: 1;
    width: 100%;
  }

  .type-item {
    display: inline-flex;
    width: calc(50% - 24px);
    margin: 3px 8px;
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

@media (max-width: 520px) {
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
}
</style>