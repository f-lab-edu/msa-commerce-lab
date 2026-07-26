package com.msa.commerce.monolith.user.adapter.in.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.msa.commerce.common.aop.ValidateResult;
import com.msa.commerce.monolith.user.adapter.in.web.UserCreateRequest;
import com.msa.commerce.monolith.user.adapter.in.web.UserLoginPolicyRequest;
import com.msa.commerce.monolith.user.adapter.in.web.UserUpdateRequest;
import com.msa.commerce.monolith.user.adapter.in.web.UserVerifyRequest;
import com.msa.commerce.monolith.user.application.port.in.UserCreateCommand;
import com.msa.commerce.monolith.user.application.port.in.UserLoginPolicyCommand;
import com.msa.commerce.monolith.user.application.port.in.UserUpdateCommand;
import com.msa.commerce.monolith.user.application.port.in.UserVerifyCommand;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    @ValidateResult
    UserCreateCommand toCommand(UserCreateRequest request);

    @Mapping(target = "userId", source = "userId")
    @ValidateResult
    UserUpdateCommand toUpdateCommand(Long userId, UserUpdateRequest request);

    @Mapping(target = "userId", source = "userId")
    @ValidateResult
    UserLoginPolicyCommand toLoginPolicyCommand(Long userId, UserLoginPolicyRequest request);

    @ValidateResult
    UserVerifyCommand toVerifyCommand(UserVerifyRequest request);

}
