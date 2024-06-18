package pers.fz.mvvm.inter;

import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.repository.PagingRepositoryImpl;

/**
 * Created by fz on 2023/12/1 16:46
 * describe :
 */
public interface PagingView extends BaseView {
    PagingRepositoryImpl createRepository();
}
