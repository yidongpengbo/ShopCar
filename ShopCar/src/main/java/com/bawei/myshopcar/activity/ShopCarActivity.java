package com.bawei.myshopcar.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bawei.myshopcar.Apis;
import com.bawei.myshopcar.Bean.ShopBean;
import com.bawei.myshopcar.Constants;
import com.bawei.myshopcar.R;
import com.bawei.myshopcar.adapter.ShopAdapter;
import com.bawei.myshopcar.presenter.IPresenterImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopCarActivity extends AppCompatActivity implements IView, View.OnClickListener {
    /**
     * 商家的适配器
     */
    private ShopAdapter mShopAdapter;
    /**
     * 全选/全不选按钮
      */
    private CheckBox mIvCircle;
    /**
     * 商家的集合
     */
    private List<ShopBean.DataBean> mList = new ArrayList<>();
    /**
     * 总价、总数
     */
    private TextView mAllPriceTxt, nSumPrice;
    /**
     * P层
     */
    private IPresenterImpl mIPresenterImpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        mIPresenterImpl = new IPresenterImpl(this);
        initView();
        getData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIPresenterImpl.onDetach();
    }

    /**
     * 获取资源ID
     */
    private void initView() {
        mIvCircle = (CheckBox) findViewById(R.id.iv_cricle);
        mAllPriceTxt = (TextView) findViewById(R.id.all_price);
        nSumPrice = (TextView) findViewById(R.id.sum_price_txt);
        mIvCircle.setOnClickListener(this);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        //RecyclerView的布局格式
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        //设置适配器
        mShopAdapter = new ShopAdapter(this);
        mRecyclerView.setAdapter(mShopAdapter);
        /**
         * ****************选中全部商家，全选/全不选按钮选中*********************
         *               根据选中的商品数量和所有的商品数量比较判断
         */

        //*****1.回调商家适配器里的接口
        mShopAdapter.setListener(new ShopAdapter.ShopCallBackListener() {
            @Override
            public void callBack(List<ShopBean.DataBean> list) {
                //在这里重新遍历已经改状态后的数据，
                // 这里不能break跳出，因为还需要计算后面点击商品的价格和数目，所以必须跑完整个循环
                double totalPrice = 0;

                //勾选商品的数量，不是该商品购买的数量
                int num = 0;
                //所有商品总数，和上面的数量做比对，如果两者相等，则说明全选
                int totalNum = 0;
                for (int a = 0; a < list.size(); a++) {
                    //获取商家里商品
                    List<ShopBean.DataBean.ListBean> listAll = list.get(a).getList();
                    //*****2.循环商品集合

                    for (int i = 0; i < listAll.size(); i++) {
                        //***3.得到所有商品的总数
                        totalNum = totalNum + listAll.get(i).getNum();
                        //****4.如果有商品选中----取选中的状态
                        if (listAll.get(i).isCheck()) {
                            //选中的商品价格
                            totalPrice = totalPrice + (listAll.get(i).getPrice() * listAll.get(i).getNum());
                            //选中商品的数量
                            num = num + listAll.get(i).getNum();
                        }
                    }
                }
                //****5.如果选中商品的数量<商品的总数量
                if (num < totalNum) {
                    //不是全部选中
                    mIvCircle.setChecked(false);
                } else {
                    //是全部选中
                    mIvCircle.setChecked(true);
                }
                //*****6.将值设置
                mAllPriceTxt.setText("合计：" + totalPrice);
                nSumPrice.setText("去结算(" + num + ")");
            }
        });
    }

    /**
     * 将地址和Bean类交给P层
     */
    private void getData() {
        Map<String, String> map = new HashMap<>();
        map.put(Constants.MAP_KEY_GET_PRODUCT_UID, "71");

        mIPresenterImpl.startRequest(Apis.URL_GET_SHOP_CAR_INFO, map, ShopBean.class);
    }

    /**
     * 通过MVP得到的数据
     * @param data
     */
    @Override
    public void showResponseData(Object data) {
        if (data instanceof ShopBean) {
            ShopBean shopBean = (ShopBean) data;
            mList = shopBean.getData();
            if (mList != null) {
                mList.remove(0);
                mShopAdapter.setList(mList);
            }
        }
    }

    @Override
    public void showResponseFail(Object data) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //全选/全不选的点击事件
            case R.id.iv_cricle:
                checkSeller(mIvCircle.isChecked());
                //刷新商家适配器
                mShopAdapter.notifyDataSetChanged();
                break;
            default:

        }
    }

    /**
     * *******************全选/全不选复选框选中**********************
     *                   1.所有商家的复选框选中
     *                   2.所有的商品复选框选中
     * 修改选中状态，获取价格和数量
     */
    private void checkSeller(boolean bool) {
        double totalPrice = 0;
        int num = 0;
        for (int a = 0; a < mList.size(); a++) {
            //****1.遍历商家，改变状态---设置商家状态为全选中
            ShopBean.DataBean dataBean = mList.get(a);
            dataBean.setCheck(bool);
                    //得到所有的商品
            List<ShopBean.DataBean.ListBean> listAll = mList.get(a).getList();
            for (int i = 0; i < listAll.size(); i++) {
                //****2.遍历商品，改变状态---设置商家的商品全部选中
                listAll.get(i).setCheck(bool);
                //计算总价
                totalPrice = totalPrice + (listAll.get(i).getPrice() * listAll.get(i).getNum());
                //计算总数量
                num = num + listAll.get(i).getNum();
            }
        }

        if (bool) {
            mAllPriceTxt.setText("合计：" + totalPrice);
            nSumPrice.setText("去结算(" + num + ")");
        } else {
            mAllPriceTxt.setText("合计：0.00");
            nSumPrice.setText("去结算(0)");
        }

    }
}
