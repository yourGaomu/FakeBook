package com.zhangzc.bookmarketbiz.Controller;

import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookmarketbiz.Dto.UserAddressDto;
import com.zhangzc.bookmarketbiz.Service.UserAddressService;
import com.zhangzc.bookmarketbiz.Vo.UserAddressVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户收货地址控制器
 */
@RestController
@RequestMapping("/market/address")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService userAddressService;

    /**
     * 获取地址列表
     * @return 地址列表
     */
    @GetMapping("/list")
    public R<List<UserAddressVo>> listAddresses() throws BizException {
        return R.success(userAddressService.listAddresses());
    }

    /**
     * 添加地址
     * @param dto 地址信息
     * @return 新地址 ID
     */
    @PostMapping("/add")
    public R<String> addAddress(@RequestBody UserAddressDto dto) throws BizException {
        return R.success(userAddressService.addAddress(dto));
    }

    /**
     * 修改地址
     * @param dto 地址信息
     * @return 成功信息
     */
    @PostMapping("/update")
    public R<String> updateAddress(@RequestBody UserAddressDto dto) throws BizException {
        userAddressService.updateAddress(dto);
        return R.success("修改成功");
    }

    /**
     * 删除地址
     * @param id 地址 ID
     * @return 成功信息
     */
    @PostMapping("/delete/{id}")
    public R<String> deleteAddress(@PathVariable("id") String id) throws BizException {
        userAddressService.deleteAddress(id);
        return R.success("删除成功");
    }
}
