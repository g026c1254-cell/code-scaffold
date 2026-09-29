<template>
  <div class="form-container">
    <section class="account-sections">
      <!-- ユーザー情報 & 残高サマリー -->
      <div class="user-summary-card">
        <div class="user-info-section">
          <img :src="getImageUrl(user.avatar)" class="user-summary-avatar" @error="handleImageError">
          <div class="user-summary-details">
            <div class="user-summary-name">{{ user.name || user.username }}</div>
            <div class="user-summary-role">{{ user.role === 'ADMIN' ? 'システム管理者' : '一般ユーザー' }}</div>
          </div>
        </div>
        <div class="user-balance-section">
          <div class="balance-title">{{ $t('common.balance') }}</div>
          <div class="balance-content">
            <span class="balance-yen-sign">¥</span>
            <span class="balance-num">{{ Number(user.account || 0).toLocaleString() }}</span>
            <span class="balance-yen-unit">{{ $t('common.yen') }}</span>
            <el-button type="success" size="mini" plain icon="el-icon-wallet" class="person-recharge-btn" @click="$router.push('/front/profile')">
              {{ $t('common.recharge') }}
            </el-button>
          </div>
        </div>
      </div>

      <el-card class="account-card">
        <el-tabs v-model="activeSection" @tab-click="handleSectionChange">
          <el-tab-pane :label="$t('common.myProducts')" name="products">
            <el-row v-if="myGoods.length" :gutter="18">
              <el-col :xs="12" :sm="8" :md="6" v-for="item in myGoods" :key="item.id" class="activity-col">
                <el-card class="activity-card" :body-style="{ padding: '0px' }" @click.native="goGoods(item.id)">
                  <img :src="getImageUrl(item.cover)" class="activity-image" @error="handleImageError">
                  <div class="activity-content">
                    <div class="activity-name">{{ item.name }}</div>
                    <div class="activity-meta">{{ item.price }}円</div>
                    <div class="activity-publisher">{{ $t('common.publisher') }}：{{ item.userName || user.name }}</div>
                    <div class="activity-actions">
                      <el-button type="primary" size="mini" plain @click.stop="openEditGoods(item)">{{ $t('common.editProduct') }}</el-button>
                      <el-button type="danger" size="mini" plain @click.stop="deleteGoods(item)">{{ $t('common.deleteProduct') }}</el-button>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
            <el-empty v-else :description="$t('common.noMyProducts')"></el-empty>
          </el-tab-pane>

          <el-tab-pane :label="$t('common.myNotices')" name="notices">
            <el-table v-if="myNotices.length" :data="myNotices" stripe>
              <el-table-column prop="name" :label="$t('common.title')" min-width="220"></el-table-column>
              <el-table-column :label="$t('common.content')" min-width="300" show-overflow-tooltip>
                <template v-slot="scope">{{ stripHtml(scope.row.content) }}</template>
              </el-table-column>
              <el-table-column prop="views" label="閲覧数" width="85" align="center">
                <template v-slot="scope">{{ scope.row.views || 0 }}</template>
              </el-table-column>
              <el-table-column prop="likes" label="いいね" width="85" align="center">
                <template v-slot="scope">{{ scope.row.likes || 0 }}</template>
              </el-table-column>
              <el-table-column prop="time" :label="$t('common.publishTime')" width="180"></el-table-column>
              <el-table-column :label="$t('common.operation')" width="100">
                <template v-slot="scope">
                  <el-button type="primary" size="mini" plain @click="openEditNotice(scope.row)">{{ $t('common.editNotice') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-else :description="$t('common.noMyNotices')"></el-empty>
          </el-tab-pane>

          <el-tab-pane :label="$t('common.collection')" name="collect">
            <el-row v-if="collects.length" :gutter="18">
              <el-col :xs="12" :sm="8" :md="6" v-for="item in collects" :key="item.id" class="activity-col">
                <el-card class="activity-card" :body-style="{ padding: '0px' }" @click.native="goGoods(item.goodsId)">
                  <img :src="getImageUrl(item.goods && item.goods.cover)" class="activity-image" @error="handleImageError">
                  <div class="activity-content">
                    <div class="activity-name">{{ item.goods && item.goods.name }}</div>
                    <div class="activity-meta">{{ item.goods && item.goods.price }}円</div>
                    <el-button type="text" class="remove-collect" @click.stop="removeCollect(item)">{{ $t('common.cancelCollection') }}</el-button>
                  </div>
                </el-card>
              </el-col>
            </el-row>
            <el-empty v-else :description="$t('common.noCollection')"></el-empty>
          </el-tab-pane>

          <el-tab-pane :label="$t('common.unpaid')" name="unpaid">
            <el-table v-if="unpaidOrders.length" :data="unpaidOrders" stripe>
              <el-table-column prop="name" :label="$t('common.productName')" min-width="160"></el-table-column>
              <el-table-column prop="price" :label="$t('common.price')" width="100"></el-table-column>
              <el-table-column prop="nums" :label="$t('common.quantity')" width="80"></el-table-column>
              <el-table-column prop="time" :label="$t('common.orderTime')" min-width="160"></el-table-column>
              <el-table-column :label="$t('common.operation')" width="100">
                <template v-slot="scope">
                  <el-button type="primary" size="mini" plain @click="payOrder(scope.row)">{{ $t('common.pay') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-else :description="$t('common.noUnpaid')"></el-empty>
          </el-tab-pane>

          <el-tab-pane :label="$t('common.purchased')" name="purchased">
            <el-table v-if="paidOrders.length" :data="paidOrders" stripe>
              <el-table-column prop="name" :label="$t('common.productName')" min-width="180"></el-table-column>
              <el-table-column prop="price" :label="$t('common.price')" width="100"></el-table-column>
              <el-table-column prop="nums" :label="$t('common.quantity')" width="80"></el-table-column>
              <el-table-column prop="time" :label="$t('common.orderTime')" min-width="160"></el-table-column>
              <el-table-column :label="$t('common.status')" width="100">
                <template v-slot="scope">
                  <el-tag type="success">{{ scope.row.state }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-else :description="$t('common.noPurchased')"></el-empty>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </section>

    <el-dialog custom-class="mobile-dialog" :title="$t('common.editProduct')" :visible.sync="editDialogVisible" width="560px" :close-on-click-modal="false">
      <el-form ref="editGoodsForm" :model="editGoodsForm" :rules="goodsRules" label-width="90px">
        <el-form-item :label="$t('common.productName')" prop="name">
          <el-input v-model="editGoodsForm.name" maxlength="100"></el-input>
        </el-form-item>
        <el-form-item :label="$t('common.productCategory')" prop="typeId" class="category-form-item">
          <div class="category-chips-grid">
            <div
              v-for="type in types"
              :key="type.id"
              class="category-chip"
              :class="{ 'is-selected': editGoodsForm.typeId === type.id }"
              @click="handleSelectCategory(type.id)"
            >
              <i class="el-icon-check check-mark" v-if="editGoodsForm.typeId === type.id"></i>
              <span class="chip-label">{{ displayTypeName(type.name) }}</span>
            </div>
          </div>
          <el-input v-model="editGoodsForm.typeId" style="display: none;"></el-input>
        </el-form-item>
        <el-form-item :label="$t('common.price')" prop="price">
          <el-input-number v-model="editGoodsForm.price" :min="0.01" :precision="2" :step="1" controls-position="right" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item :label="$t('common.stock')" prop="store">
          <el-input-number v-model="editGoodsForm.store" :min="1" :step="1" controls-position="right" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="商品画像" prop="cover">
          <div class="goods-upload-box">
            <el-upload
              action=""
              :http-request="uploadEditCover"
              :show-file-list="false"
              accept="image/*"
              :before-upload="beforeCoverUpload">
              <div v-if="editGoodsForm.cover" class="cover-preview-wrapper">
                <img :src="getImageUrl(editGoodsForm.cover)" class="edit-cover-preview">
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
            v-model="editGoodsForm.descr"
            maxlength="1000"
            show-word-limit
            placeholder="商品の状態（キズ・汚れの有無）、使用期間、サイズ、付属品などを詳しく記入してください"
          ></el-input>
        </el-form-item>
        <el-form-item label="購入注意事項" prop="purchaseNotice">
          <el-input
            type="textarea"
            :rows="3"
            v-model="editGoodsForm.purchaseNotice"
            maxlength="500"
            show-word-limit
            placeholder="例：八王子キャンパス内での手渡し希望、平日の夕方対応可能、即購入OK、返品不可など"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="editDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitEditGoods">{{ $t('common.republishProduct') }}</el-button>
      </div>
    </el-dialog>

    <el-dialog custom-class="mobile-dialog" :title="$t('common.editNotice')" :visible.sync="noticeDialogVisible" width="460px" :close-on-click-modal="false">
      <el-form ref="editNoticeForm" :model="editNoticeForm" :rules="noticeRules" label-width="80px">
        <el-form-item :label="$t('common.title')" prop="name">
          <el-input v-model="editNoticeForm.name" maxlength="100"></el-input>
        </el-form-item>
        <el-form-item :label="$t('common.content')" prop="content">
          <div class="notice-editor">
            <Toolbar :editor="noticeEditor" :defaultConfig="toolbarConfig" mode="default" />
            <Editor
              v-model="editNoticeForm.content"
              :defaultConfig="editorConfig"
              mode="default"
              @onCreated="onNoticeEditorCreated"
            />
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="noticeDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitEditNotice">{{ $t('common.save') }}</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'

export default {
  name: "Person",
  components: { Editor, Toolbar },
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      activeSection: 'products',
      myGoods: [],
      myNotices: [],
      collects: [],
      orders: [],
      types: [],
      editDialogVisible: false,
      editGoodsForm: {},
      noticeDialogVisible: false,
      editNoticeForm: {},
      editor: null,
      noticeEditor: null,
      toolbarConfig: {},
      editorConfig: {
        placeholder: '商品の状態や詳細説明を入力してください',
        MENU_CONF: {
          uploadImage: {
            server: '',
            fieldName: 'file',
            headers: {},
            allowedFileTypes: ['image/*'],
            customInsert: (res, insertFn) => {
              let url = ''
              if (res && res.data) {
                if (typeof res.data === 'string') {
                  url = res.data
                } else if (Array.isArray(res.data) && res.data.length > 0) {
                  url = typeof res.data[0] === 'string' ? res.data[0] : res.data[0].url
                } else if (res.data.url) {
                  url = res.data.url
                }
              } else if (res && res.url) {
                url = res.url
              }
              if (url) {
                insertFn(this.getImageUrl(url))
              }
            }
          }
        }
      },
      goodsRules: {
        name: [{ required: true, message: '商品名を入力してください', trigger: 'blur' }],
        typeId: [{ required: true, message: '商品カテゴリを選択してください', trigger: 'change' }],
        price: [{ required: true, message: '価格を入力してください', trigger: 'change' }]
      },
      noticeRules: {
        name: [{ required: true, message: 'お知らせのタイトルを入力してください', trigger: 'blur' }],
        content: [{ required: true, message: 'お知らせの内容を入力してください', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.configureEditor()
    if (!this.user.id || !this.user.token) {
      this.$router.push('/login')
      return
    }
    this.loadUser()
    this.loadActivity()
  },
  beforeDestroy() {
    if (this.editor) this.editor.destroy()
    if (this.noticeEditor) this.noticeEditor.destroy()
  },
  methods: {
    loadUser() {
      if (!this.user.id) return
      this.$request.get('/user/selectById/' + this.user.id).then(res => {
        if (res.code === '200' && res.data) {
          const token = this.user.token
          this.user = Object.assign({}, this.user, res.data, { token })
          localStorage.setItem('user', JSON.stringify(this.user))
        }
      }).catch(() => {})
    },
    configureEditor() {
      this.editorConfig.MENU_CONF.uploadImage.server = this.$baseUrl + '/file/editor/upload'
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      this.editorConfig.MENU_CONF.uploadImage.headers = { token: user.token || '' }
      this.editorConfig.MENU_CONF.uploadImage.customInsert = (res, insertFn) => {
        let url = ''
        if (res && res.data) {
          if (typeof res.data === 'string') {
            url = res.data
          } else if (Array.isArray(res.data) && res.data.length > 0) {
            url = typeof res.data[0] === 'string' ? res.data[0] : res.data[0].url
          } else if (res.data.url) {
            url = res.data.url
          }
        } else if (res && res.url) {
          url = res.url
        }
        if (url) {
          insertFn(this.getImageUrl(url))
        }
      }
    },
    loadActivity() {
      this.loadTypes()
      this.loadMyGoods()
      this.loadMyNotices()
      this.loadCollects()
      this.loadOrders()
    },
    loadTypes() {
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
    handleSelectCategory(id) {
      this.editGoodsForm.typeId = id
      this.$nextTick(() => {
        if (this.$refs.editGoodsForm) {
          this.$refs.editGoodsForm.validateField('typeId')
        }
      })
    },
    openEditGoods(item) {
      let descr = item.descr || ''
      let purchaseNotice = ''
      if (item.content && item.content.indexOf('<!--PURCHASE_NOTICE_START-->') !== -1) {
        const parts = item.content.split('<!--PURCHASE_NOTICE_START-->')
        descr = parts[0].trim() || descr
        purchaseNotice = parts[1].trim()
      }
      this.editGoodsForm = {
        id: item.id,
        name: item.name,
        typeId: item.typeId,
        price: item.price,
        store: Math.max(1, item.store || 1),
        cover: item.cover,
        descr: descr,
        purchaseNotice: purchaseNotice
      }
      this.editDialogVisible = true
      this.$nextTick(() => this.$refs.editGoodsForm && this.$refs.editGoodsForm.clearValidate())
    },
    submitEditGoods() {
      this.$refs.editGoodsForm.validate(valid => {
        if (!valid) return
        let combinedContent = this.editGoodsForm.descr || ''
        if (this.editGoodsForm.purchaseNotice) {
          combinedContent += '\n<!--PURCHASE_NOTICE_START-->\n' + this.editGoodsForm.purchaseNotice
        }
        const form = Object.assign({}, this.editGoodsForm, {
          descr: this.editGoodsForm.descr,
          content: combinedContent
        })
        this.$request.put('/goods/update', form).then(res => {
          if (res.code === '200') {
            this.$message.success(this.$t('common.republishSuccess'))
            this.editDialogVisible = false
            this.loadMyGoods()
          } else {
            this.$message.error(res.msg || this.$t('common.republishFailed'))
          }
        })
      })
    },
    beforeCoverUpload(file) {
      const isImage = /^image\//.test(file.type)
      if (!isImage) this.$message.error(this.$t('common.imageOnly'))
      return isImage
    },
    uploadEditCover(options) {
      const formData = new FormData()
      formData.append('file', options.file)
      this.$request.post('/file/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      }).then(res => {
        if (res.code === '200') {
          this.editGoodsForm.cover = res.data
          options.onSuccess(res)
        } else {
          options.onError(new Error(res.msg || this.$t('common.imageUploadFailed')))
        }
      }).catch(options.onError)
    },
    onEditorCreated(editor) {
      this.editor = Object.seal(editor)
    },
    onNoticeEditorCreated(editor) {
      this.noticeEditor = Object.seal(editor)
    },
    stripHtml(value) {
      if (!value) return ''
      const container = document.createElement('div')
      container.innerHTML = value
      return (container.textContent || container.innerText || '').replace(/\s+/g, ' ').trim().slice(0, 300)
    },
    deleteGoods(item) {
      this.$confirm(this.$t('common.confirmDeleteProduct'), this.$t('common.deleteProduct'), { type: 'warning' }).then(() => {
        this.$request.delete('/goods/delete?id=' + item.id).then(res => {
          if (res.code === '200') {
            this.$message.success(this.$t('common.deleteSuccess'))
            this.loadMyGoods()
          } else {
            this.$message.error(res.msg || this.$t('common.deleteFailed'))
          }
        })
      }).catch(() => {})
    },
    loadMyGoods() {
      this.$request.get('/goods/myGoods').then(res => {
        this.myGoods = Array.isArray(res.data) ? res.data : []
      }).catch(() => {
        this.myGoods = []
      })
    },
    loadMyNotices() {
      this.$request.get('/notice/myNotices').then(res => {
        this.myNotices = Array.isArray(res.data) ? res.data : []
      }).catch(() => {
        this.myNotices = []
      })
    },
    openEditNotice(item) {
      this.editNoticeForm = {
        id: item.id,
        name: item.name || '',
        content: item.content || ''
      }
      this.noticeDialogVisible = true
      this.$nextTick(() => this.$refs.editNoticeForm && this.$refs.editNoticeForm.clearValidate())
    },
    submitEditNotice() {
      this.$refs.editNoticeForm.validate(valid => {
        if (!valid) return
        const hasText = !!this.stripHtml(this.editNoticeForm.content)
        const hasImg = /<img\b/i.test(this.editNoticeForm.content)
        if (!hasText && !hasImg) {
          this.$message.error(this.$t('common.content'))
          return
        }
        this.$request.put('/notice/update', this.editNoticeForm).then(res => {
          if (res.code === '200') {
            this.$message.success(this.$t('common.noticeUpdateSuccess'))
            this.noticeDialogVisible = false
            this.loadMyNotices()
          } else {
            this.$message.error(res.msg || this.$t('common.noticeUpdateFailed'))
          }
        })
      })
    },
    loadCollects() {
      this.$request.get('/collect/myCollect').then(res => {
        this.collects = Array.isArray(res.data) ? res.data : []
      })
    },
    loadOrders() {
      this.$request.get('/orders/selectPage', {
        params: { pageNum: 1, pageSize: 100, name: '', orderNo: '' }
      }).then(res => {
        this.orders = res.data && Array.isArray(res.data.records) ? res.data.records : []
      })
    },
    handleSectionChange() {
      this.loadActivity()
    },
    goGoods(id) {
      if (id) this.$router.push({ name: 'GoodsDetail', query: { id } })
    },
    removeCollect(item) {
      this.$request.delete('/collect/delete?id=' + item.id).then(res => {
        if (res.code === '200') {
          this.$message.success(this.$t('common.cancelCollection'))
          this.loadCollects()
        } else {
          this.$message.error(res.msg || this.$t('common.cancelCollection'))
        }
      })
    },
    payOrder(order) {
      this.$confirm(this.$t('common.confirmPay'), this.$t('common.pay'), { type: 'warning' }).then(() => {
        this.$request.post('/orders/pay', { id: order.id }).then(res => {
          if (res.code === '200') {
            this.$message.success(this.$t('common.paySuccess'))
            this.loadOrders()
            this.loadUser()
          } else {
            let msg = res.msg || this.$t('common.payFailed')
            if (msg.indexOf('余额不足') !== -1 || msg.indexOf('残高が不足') !== -1) {
              msg = '残高が不足しています。チャージしてください'
            }
            this.$message.error(msg)
          }
        })
      }).catch(() => {})
    },
    getImageUrl(url) {
      if (!url) return require('@/assets/empty.svg')
      if (/^https?:\/\//i.test(url) || /^data:/i.test(url)) return url
      return this.$baseUrl + (url.startsWith('/') ? '' : '/') + url
    },
    handleImageError(event) {
      event.target.src = require('@/assets/empty.svg')
    }
  },
  computed: {
    unpaidOrders() {
      return this.orders.filter(order => order.state !== '已支付')
    },
    paidOrders() {
      return this.orders.filter(order => order.state === '已支付')
    }
  }
}
</script>

<style scoped>
.form-container {
  display: flex;
  justify-content: center;
  padding: 18px 10px;
  box-sizing: border-box;
}

.account-sections {
  width: min(1180px, 94%);
  margin: 0 auto 40px;
}

/* ユーザー概要 & 残高バー */
.user-summary-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ffffff;
  border: 1px solid rgba(226, 232, 240, .8);
  border-radius: 16px;
  padding: 18px 24px;
  margin-bottom: 20px;
  box-shadow: 0 10px 25px -5px rgba(15, 23, 42, .06);
  flex-wrap: wrap;
  gap: 16px;
}

.user-info-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-summary-avatar {
  width: 58px;
  height: 58px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #fed7aa;
  background: #fff7ed;
}

.user-summary-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-summary-name {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

.user-summary-role {
  font-size: 12px;
  color: #ea6b1f;
  background: #fff7ed;
  border: 1px solid #fed7aa;
  padding: 2px 8px;
  border-radius: 10px;
  width: fit-content;
  font-weight: 600;
}

.user-balance-section {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.balance-title {
  font-size: 12px;
  color: #64748b;
  font-weight: 600;
}

.balance-content {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.balance-yen-sign {
  font-size: 16px;
  font-weight: 700;
  color: #16a34a;
}

.balance-num {
  font-size: 22px;
  font-weight: 700;
  color: #15803d;
}

.balance-yen-unit {
  font-size: 13px;
  color: #16a34a;
  font-weight: 600;
  margin-right: 8px;
}

.person-recharge-btn {
  border-color: #86efac !important;
  color: #15803d !important;
  background: #f0fdf4 !important;
}

.person-recharge-btn:hover {
  background: #dcfce7 !important;
  border-color: #4ade80 !important;
}

.account-card {
  border-radius: 16px;
  border: 1px solid rgba(226, 232, 240, .8);
  box-shadow: 0 10px 25px -5px rgba(15, 23, 42, .08);
}

.activity-col {
  margin-top: 14px;
}

.activity-card {
  overflow: hidden;
  border-radius: 12px;
  cursor: pointer;
  transition: transform .2s ease, box-shadow .2s ease;
}

.activity-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 22px rgba(15, 23, 42, .12);
}

.activity-image {
  display: block;
  width: 100%;
  height: 150px;
  object-fit: contain;
  background: #f8fafc;
}

.activity-content {
  padding: 12px;
}

.activity-name {
  overflow: hidden;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-meta {
  margin-top: 8px;
  color: #ff7e29;
  font-weight: 600;
}

.activity-publisher {
  margin-top: 7px;
  color: #909399;
  font-size: 12px;
}

.activity-actions {
  display: flex;
  gap: 6px;
  margin-top: 10px;
}

.activity-actions .el-button {
  margin: 0;
}

.edit-cover-preview {
  display: block;
  width: 110px;
  height: 110px;
  border-radius: 8px;
  object-fit: contain;
  background: #f8fafc;
}

.edit-cover-uploader {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 110px;
  height: 110px;
  border: 1px dashed #c0c4cc;
  border-radius: 6px;
  color: #909399;
  font-size: 24px;
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

.remove-collect {
  padding: 4px 0;
  color: #ff7e29;
}

@media (max-width: 700px) {
  .form-container {
    padding: 10px 6px 24px;
  }

  .account-sections {
    width: 100%;
  }

  .user-summary-card {
    padding: 14px;
    flex-direction: column;
    align-items: stretch;
  }

  .user-balance-section {
    align-items: flex-start;
    padding-top: 10px;
    border-top: 1px dashed #f1f5f9;
  }

  .account-card >>> .el-tabs__nav-wrap {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .account-card >>> .el-tabs__nav {
    display: flex;
    width: max-content;
    min-width: 100%;
  }

  .account-card >>> .el-tabs__item {
    flex: none;
    padding: 0 14px;
    font-size: 13px;
    white-space: nowrap;
    text-align: center;
  }

  .activity-actions {
    flex-direction: column;
  }

  .activity-actions .el-button {
    width: 100%;
  }

  .activity-image {
    height: 160px;
    object-fit: contain;
  }

  .account-card >>> .el-table {
    width: 100%;
    overflow-x: auto;
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
  }

  /* 移动端弹窗响应式样式 */
  .mobile-dialog >>> .el-dialog {
    width: 94% !important;
    max-width: 520px;
    margin: 20px auto !important;
    border-radius: 14px;
  }

  .mobile-dialog >>> .el-dialog__body {
    padding: 16px 14px;
    max-height: 68vh;
    overflow-y: auto;
  }

  .mobile-dialog >>> .el-form-item__label {
    float: none;
    display: block;
    width: 100% !important;
    text-align: left;
    padding: 0 0 6px;
    line-height: 1.3;
  }

  .mobile-dialog >>> .el-form-item__content {
    margin-left: 0 !important;
  }
}

.goods-upload-box {
  width: 100%;
}
.cover-preview-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
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
