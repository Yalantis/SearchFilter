package com.yalantis.fitfilter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

/**
 * Created by galata on 16.09.16.
 */
public class QuestionsAdapter extends RecyclerView.Adapter<QuestionsAdapter.ViewHolder> {

    private List<Question> mQuestions;
    private Context mContext;

    public QuestionsAdapter(Context context, List<Question> questions) {
        mContext = context;
        mQuestions = questions;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, null, false));
    }

    public List<Question> getQuestions() {
        return mQuestions;
    }

    public void setQuestions(List<Question> questions) {
        this.mQuestions = questions;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Question question = mQuestions.get(position);

        holder.avatar.setImageURI(question.getAuthorAvatar());
        holder.textAuthorName.setText(question.getAuthorName());
        holder.textJobTitle.setText(question.getAuthorJobTitle());
        holder.textDate.setText(question.getDate());
        holder.textQuestion.setText(question.getText());
        Tag firstTag = question.getTags().get(0);
        holder.firstFilter.setText(firstTag.getText());
        Tag secondTag = question.getTags().get(1);
        holder.secondFilter.setText(secondTag.getText());

        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(1000);
        drawable.setColor(firstTag.getColor());
        holder.firstFilter.setBackgroundDrawable(drawable);
        GradientDrawable drawable1 = new GradientDrawable();
        drawable1.setCornerRadius(1000);
        drawable1.setColor(secondTag.getColor());
        holder.secondFilter.setBackgroundDrawable(drawable1);
    }

    private int getColor(int color) {
        return ContextCompat.getColor(mContext, color);
    }

    @Override
    public int getItemCount() {
        return mQuestions.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView textAuthorName;
        TextView textJobTitle;
        TextView textDate;
        TextView textQuestion;
        TextView firstFilter;
        TextView secondFilter;
        SimpleDraweeView avatar;

        public ViewHolder(View itemView) {
            super(itemView);

            textAuthorName = (TextView) itemView.findViewById(R.id.text_name);
            textJobTitle = (TextView) itemView.findViewById(R.id.text_job_title);
            textDate = (TextView) itemView.findViewById(R.id.text_date);
            textQuestion = (TextView) itemView.findViewById(R.id.text_question);
            firstFilter = (TextView) itemView.findViewById(R.id.filter_first);
            secondFilter = (TextView) itemView.findViewById(R.id.filter_second);
            avatar = (SimpleDraweeView) itemView.findViewById(R.id.avatar);
        }
    }
}
