package com.zhangzc.bookmarketbiz.Service;

import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookmarketbiz.Dto.UserAddressDto;
import com.zhangzc.bookmarketbiz.Vo.UserAddressVo;

import java.util.List;

/**
 * 用户地址服务接口
 */
public interface UserAddressService {

    /**
     * 获取当前用户的收货地址列表
     * @return 地址列表
     */
    List<UserAddressVo> listAddresses() throws BizException;

    /**
     * 添加收货地址
     * @param dto 地址信息
     * @return 新地址 ID
     */
    String addAddress(UserAddressDto dto) throws BizException;

    /**
     * 修改收货地址
     * @param dto 地址信息
     */
    void updateAddress(UserAddressDto dto) throws BizException;

    /**
     * 删除收货地址
     * @param id 地址 ID
     */
    void deleteAddress(String id) throws BizException;
}
