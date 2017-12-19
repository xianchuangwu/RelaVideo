package video.com.relavideolibrary;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by chad
 * Time 17/6/29
 * Email: wuxianchuang@foxmail.com
 * Description: TODO 实现上滑自动加载更多
 */

public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter<BaseViewHolder> {

    protected List<T> mData;
    protected Context mContext;
    private int mLayoutId;
    private RecyclerView mRecyclerView;

    public BaseRecyclerAdapter(@LayoutRes int layoutId, RecyclerView recyclerView, Collection<T> list) {
        this.mLayoutId = layoutId;
        this.mContext = recyclerView.getContext();
        this.mRecyclerView = recyclerView;
        mData = list == null ? new ArrayList<T>() : (List<T>) list;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(mContext).inflate(mLayoutId, parent, false);
        return new BaseViewHolder(root);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        autoLoadMore(position);
        T item = mData.get(position);
        dataBinding(holder, item, position);
    }

    public abstract void dataBinding(BaseViewHolder holder, T item, int position);

    private void autoLoadMore(int position) {
        if (position == getItemCount() - 1) {
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    if (mOnLoadListener != null) mOnLoadListener.onLoadMore();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void removeData(@IntRange(from = 0) int position) {
        mData.remove(position);
        notifyItemRemoved(position);
        compatibilityDataSizeChanged(0);
        notifyItemRangeChanged(position, mData.size() - position);
    }

    public void setData(List<T> data) {
        this.mData = data == null ? new ArrayList<T>() : data;
        this.notifyDataSetChanged();
    }

    public void setItem(@IntRange(from = 0) int index, @NonNull T data) {
        mData.set(index, data);
        notifyItemChanged(index);
    }

    public void addData(@IntRange(from = 0) int position, @NonNull List<T> data) {
        mData.addAll(position, data);
        notifyItemRangeInserted(position, data.size());
        compatibilityDataSizeChanged(data.size());
    }

    public void addData(@NonNull List<T> data) {
        int start = mData.size();
        mData.addAll(data);
        notifyItemRangeInserted(start, data.size());
        compatibilityDataSizeChanged(data.size());
    }

    public void addItem(@IntRange(from = 0) int position, @NonNull T data) {
        mData.add(position, data);
        notifyItemInserted(position);
        compatibilityDataSizeChanged(1);
    }

    public void addItem(@NonNull T data) {
        mData.add(data);
        int position = mData.size();
        notifyItemInserted(position);
        compatibilityDataSizeChanged(1);
    }

    private void compatibilityDataSizeChanged(int size) {
        final int dataSize = mData == null ? 0 : mData.size();
        if (dataSize == size) {
            notifyDataSetChanged();
        }
    }

    @NonNull
    public List<T> getData() {
        return mData;
    }

    @Nullable
    public T getItem(@IntRange(from = 0) int position) {
        if (position < mData.size())
            return mData.get(position);
        else
            return null;
    }

    private OnLoadMoreListener mOnLoadListener;

    public void setOnLoadListener(OnLoadMoreListener onLoadListener) {
        this.mOnLoadListener = onLoadListener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }
}
