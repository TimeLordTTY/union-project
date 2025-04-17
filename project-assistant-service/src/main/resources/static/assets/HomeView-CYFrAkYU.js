import{m as a,d as t,e as s}from"./elementPlus-D3jm9WeK.js";import{u as e}from"./project-B2gwCvL8.js";import{x as l,r as d,c as o,k as c,y as i,P as r,H as u,ag as n,A as v,M as m,G as p,L as f,u as _,z as h}from"./vue-CYQ3jvsg.js";import{_ as w}from"./index-CSUhv2L2.js";import"./index-fZjk1EK6.js";const y={class:"home-container"},k={key:0,class:"loading-container"},g={key:1,class:"stats-content"},x={class:"stat-item"},b={class:"stat-value"},j={class:"stat-item status-active"},C={class:"stat-value"},$={class:"stat-item status-completed"},P={class:"stat-value"},A={class:"stat-item status-cancelled"},z={class:"stat-value"},H={class:"stat-item status-expired"},I={class:"stat-value"},D={class:"card-header"},E={key:0,class:"loading-container"},G={key:1},L={class:"tools-container"},M=w(l({__name:"HomeView",setup(l){const w=e(),M=d(!0),S=o((()=>w.projectStatistics)),V=o((()=>w.activeProjects));return c((async()=>{M.value=!0;try{await w.fetchAllProjects()}finally{M.value=!1}})),(e,l)=>{const d=n("el-card"),o=n("el-col"),c=n("el-row"),w=n("el-skeleton"),W=n("el-button"),q=n("el-empty"),B=n("el-table-column"),F=n("el-table"),J=n("el-icon");return h(),i("div",y,[r(c,{gutter:20},{default:u((()=>[r(o,{span:24},{default:u((()=>[r(d,{shadow:"hover",class:"welcome-card"},{default:u((()=>l[4]||(l[4]=[v("div",{class:"welcome-content"},[v("h1",null,"欢迎使用项目管理小助手"),v("p",null,"一个用于项目管理和日期规划的应用，集成了项目管理、金额转换、文档生成和文本纠错等功能。")],-1)]))),_:1})])),_:1})])),_:1}),r(c,{gutter:20,class:"mt-20"},{default:u((()=>[r(o,{span:12},{default:u((()=>[r(d,{shadow:"hover",class:"stats-card"},{header:u((()=>l[5]||(l[5]=[v("div",{class:"card-header"},[v("h3",null,"项目统计")],-1)]))),default:u((()=>[M.value?(h(),i("div",k,[r(w,{rows:4,animated:""})])):(h(),i("div",g,[r(c,{gutter:20},{default:u((()=>[r(o,{span:12},{default:u((()=>[v("div",x,[v("div",b,m(S.value.total),1),l[6]||(l[6]=v("div",{class:"stat-label"},"总项目数",-1))])])),_:1}),r(o,{span:12},{default:u((()=>[v("div",j,[v("div",C,m(S.value.active),1),l[7]||(l[7]=v("div",{class:"stat-label"},"进行中",-1))])])),_:1})])),_:1}),r(c,{gutter:20,class:"mt-10"},{default:u((()=>[r(o,{span:8},{default:u((()=>[v("div",$,[v("div",P,m(S.value.completed),1),l[8]||(l[8]=v("div",{class:"stat-label"},"已完成",-1))])])),_:1}),r(o,{span:8},{default:u((()=>[v("div",A,[v("div",z,m(S.value.cancelled),1),l[9]||(l[9]=v("div",{class:"stat-label"},"已取消",-1))])])),_:1}),r(o,{span:8},{default:u((()=>[v("div",H,[v("div",I,m(S.value.expired),1),l[10]||(l[10]=v("div",{class:"stat-label"},"已过期",-1))])])),_:1})])),_:1})]))])),_:1})])),_:1}),r(o,{span:12},{default:u((()=>[r(d,{shadow:"hover"},{header:u((()=>[v("div",D,[l[12]||(l[12]=v("h3",null,"近期项目",-1)),r(W,{type:"primary",size:"small",onClick:l[0]||(l[0]=a=>e.$router.push("/projects"))},{default:u((()=>l[11]||(l[11]=[f(" 查看全部 ")]))),_:1})])])),default:u((()=>[M.value?(h(),i("div",E,[r(w,{rows:5,animated:""})])):(h(),i("div",G,[0===V.value.length?(h(),p(q,{key:0,description:"暂无进行中的项目"})):(h(),p(F,{key:1,data:V.value.slice(0,5),style:{width:"100%"}},{default:u((()=>[r(B,{prop:"name",label:"项目名称"}),r(B,{prop:"onlineDate",label:"上网日期",width:"120"}),r(B,{fixed:"right",label:"操作",width:"120"},{default:u((a=>[r(W,{link:"",type:"primary",onClick:t=>e.$router.push(`/project/edit/${a.row.id}`)},{default:u((()=>l[13]||(l[13]=[f(" 查看 ")]))),_:2},1032,["onClick"])])),_:1})])),_:1},8,["data"]))]))])),_:1})])),_:1})])),_:1}),r(c,{gutter:20,class:"mt-20"},{default:u((()=>[r(o,{span:24},{default:u((()=>[r(d,{shadow:"hover"},{header:u((()=>l[14]||(l[14]=[v("div",{class:"card-header"},[v("h3",null,"快捷工具")],-1)]))),default:u((()=>[v("div",L,[r(c,{gutter:20},{default:u((()=>[r(o,{xs:24,sm:12,md:8},{default:u((()=>[r(d,{shadow:"hover",class:"tool-card",onClick:l[1]||(l[1]=a=>e.$router.push("/tools/amount-convert"))},{default:u((()=>[r(J,{class:"tool-icon"},{default:u((()=>[r(_(a))])),_:1}),l[15]||(l[15]=v("div",{class:"tool-title"},"金额转换",-1)),l[16]||(l[16]=v("div",{class:"tool-desc"},"实现数字金额与中文大写金额的互相转换",-1))])),_:1})])),_:1}),r(o,{xs:24,sm:12,md:8},{default:u((()=>[r(d,{shadow:"hover",class:"tool-card",onClick:l[2]||(l[2]=a=>e.$router.push("/tools/doc-generator"))},{default:u((()=>[r(J,{class:"tool-icon"},{default:u((()=>[r(_(t))])),_:1}),l[17]||(l[17]=v("div",{class:"tool-title"},"文档生成",-1)),l[18]||(l[18]=v("div",{class:"tool-desc"},"根据Word或Excel模板和数据文件生成新的文档",-1))])),_:1})])),_:1}),r(o,{xs:24,sm:12,md:8},{default:u((()=>[r(d,{shadow:"hover",class:"tool-card",onClick:l[3]||(l[3]=a=>e.$router.push("/tools/text-corrector"))},{default:u((()=>[r(J,{class:"tool-icon"},{default:u((()=>[r(_(s))])),_:1}),l[19]||(l[19]=v("div",{class:"tool-title"},"文本纠错",-1)),l[20]||(l[20]=v("div",{class:"tool-desc"},"使用百度API进行文本纠错，显示纠错前后的内容对比",-1))])),_:1})])),_:1})])),_:1})])])),_:1})])),_:1})])),_:1})])}}}),[["__scopeId","data-v-3d70074c"]]);export{M as default};
