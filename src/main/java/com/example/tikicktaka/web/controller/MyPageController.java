package com.example.tikicktaka.web.controller;

import com.example.tikicktaka.apiPayload.ApiResponse;
import com.example.tikicktaka.apiPayload.code.status.ErrorStatus;
import com.example.tikicktaka.apiPayload.code.status.SuccessStatus;
import com.example.tikicktaka.apiPayload.exception.handler.MemberHandler;
import com.example.tikicktaka.converter.member.MemberConverter;
import com.example.tikicktaka.domain.mapping.member.MemberTeam;
import com.example.tikicktaka.domain.member.Member;
import com.example.tikicktaka.domain.member.RegisterSeller;
import com.example.tikicktaka.service.memberService.MemberCommandService;
import com.example.tikicktaka.service.memberService.MemberQueryService;
import com.example.tikicktaka.web.dto.member.MemberRequestDTO;
import com.example.tikicktaka.web.dto.member.MemberResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@io.swagger.v3.oas.annotations.tags.Tag(name = "MyPage", description = "MyPage 관련 API")
@RequestMapping("/api/mypage")
public class MyPageController {

    private final MemberCommandService memberCommandService;
    private final MemberQueryService memberQueryService;

    @GetMapping("/my/profile")
    @Operation(summary = "나의 프로필 조회 API", description = "나의 프로필 정보 조회를 위한 API이다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ApiResponse<MemberResponseDTO.memberProfileDTO> memberProfile(Authentication authentication){

        Member member = memberQueryService.findMemberById(Long.valueOf(authentication.getName().toString())).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        return ApiResponse.onSuccess(MemberConverter.memberProfileDTO(member));
    }

    @PutMapping(value = "/profile-image/upload", consumes = "multipart/form-data")
    @Operation(summary = "마이페이지 프로필 사진 등록 api", description = "request : 프로필 이미지")
    public ApiResponse<MemberResponseDTO.ProfileModifyResultDTO> profileModify(@RequestParam("profile") MultipartFile profile,
                                                                               Authentication authentication) {
        Member member = memberQueryService.findMemberById(Long.valueOf(authentication.getName().toString())).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Member modifyMember = memberCommandService.profileImageUpload(profile, member);

        return ApiResponse.onSuccess(MemberConverter.toProfileModify(modifyMember));
    }

    @PutMapping(value = "/profile/modify")
    @Operation(summary = "프로필 수정 api", description = "request : 닉네임, 전화번호, 생년월일")
    public ApiResponse<MemberResponseDTO.UpdateProfileResultDTO> updateProfile(@RequestBody MemberRequestDTO.UpdateMemberDTO request,
                                                                               Authentication authentication){

        Member member = memberQueryService.findMemberById(Long.valueOf(authentication.getName().toString())).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Member updateMember = memberCommandService.modifyProfile(request, member);

        return ApiResponse.onSuccess(MemberConverter.toProfileUpdate(updateMember));
    }

    @PostMapping(value = "/teams/{teamId}")
    @Operation(summary = "선호 구단 등록 API", description = "request : 팀 id.")
    public ApiResponse<MemberResponseDTO.MemberPreferTeamDTO> preferTeam(@RequestParam("teamId") Long teamId,
                                                                         Authentication authentication){
        Member member = memberQueryService.findMemberById(Long.valueOf(authentication.getName().toString())).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        MemberTeam memberTeam = memberCommandService.setPreferTeam(member, teamId);
        return ApiResponse.onSuccess(MemberConverter.toMemberPreferTeamDTO(memberTeam));
    }

    @DeleteMapping(value = "/delete")
    @Operation(summary = "회원 탈퇴 api")
    public ApiResponse<?> deleteMember(Authentication authentication){

        Member member = memberQueryService.findMemberById(Long.valueOf(authentication.getName().toString())).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        memberCommandService.deleteMember(member.getId());
        return ApiResponse.of(SuccessStatus.MEMBER_DELETE_SUCCESS, null);
    }

    @PutMapping(value = "/modify/role/seller/{memberId}")
    @Operation(summary = "판매자 변경 api", description = "request: 판매자로 변경할 멤버의 id를 입력하면 됩니다.")
    public ApiResponse<MemberResponseDTO.ModifySellerResultDTO> modifySeller(@PathVariable Long memberId){
        Member member = memberQueryService.findMemberById(memberId).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Member modifySellerMember = memberCommandService.modifySeller(member.getId());
        return ApiResponse.onSuccess(MemberConverter.toModifySellerResultDTO(modifySellerMember));
    }

    @PostMapping(value = "/register/seller")
    @Operation(summary = "판매자 신청 api", description = "request: 등록 정보를 입력해주시면 됩니다.")
    public ApiResponse<MemberResponseDTO.RegisterSellerResultDTO> registerSeller(@RequestBody MemberRequestDTO.RegisterSellerDTO request,
                                                                                 Authentication authentication){
        Member member = memberQueryService.findMemberById(Long.valueOf(authentication.getName().toString())).orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        RegisterSeller registerSeller = memberCommandService.registerSeller(request,member);
        return ApiResponse.onSuccess(MemberConverter.toRegisterSellerResultDTO(registerSeller));
    }
}
