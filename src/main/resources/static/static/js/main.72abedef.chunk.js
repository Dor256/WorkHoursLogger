(window["webpackJsonpwork-logger-client"]=window["webpackJsonpwork-logger-client"]||[]).push([[0],{13:function(e,t,n){e.exports=n(36)},35:function(e,t,n){},36:function(e,t,n){"use strict";n.r(t);var a=n(0),o=n.n(a),r=n(11),c=n.n(r),l=n(12),u=n.n(l).a.create({baseURL:"https://work-logger-app.herokuapp.com"}),i=function(){return o.a.createElement("button",{className:"button",onClick:function(){u.post("/log",{dateString:(new Date).toString()})}},"Enter")},s=function(){return o.a.createElement("button",{className:"button",onClick:function(){u.put("/log",{dateString:(new Date).toString()})}},"Exit")},g=function(){return o.a.createElement("button",{className:"button",onClick:function(){u.get("/log",{params:{dateString:(new Date).toString()}})}},"Send Log")},m=(n(35),function(){return o.a.createElement("div",{className:"container"},o.a.createElement("h1",{className:"heading"},o.a.createElement("img",{className:"techsee-icon",src:"".concat("","/icon.png"),alt:""})," Work Logger"),o.a.createElement(i,null),o.a.createElement(s,null),o.a.createElement(g,null))}),p=function(){return o.a.createElement(m,null)};c.a.render(o.a.createElement(p,null),document.querySelector("#root"))}},[[13,1,2]]]);
//# sourceMappingURL=main.72abedef.chunk.js.map