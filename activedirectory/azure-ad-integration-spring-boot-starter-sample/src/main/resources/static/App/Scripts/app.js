'use strict';
angular.module('todoApp', ['ngRoute', 'AdalAngular'])
    .config(['$routeProvider', '$httpProvider', 'adalAuthenticationServiceProvider', function ($routeProvider, $httpProvider, adalProvider) {

        $routeProvider.when("/Home", {
            controller: "homeCtrl",
            templateUrl: "/App/Views/Home.html",
        }).when("/TodoList", {
            controller: "todoListCtrl",
            templateUrl: "/App/Views/TodoList.html",
            requireADLogin: true,
        }).when("/UserData", {
            controller: "userDataCtrl",
            templateUrl: "/App/Views/UserData.html",
        }).otherwise({redirectTo: "/Home"});

        adalProvider.init(
            {
                instance: 'https://login.microsoftonline.com/',
                tenant: 'zhijunzhaooutlook.onmicrosoft.com',
                clientId: '7819c05b-b98a-4c24-9ab6-7e63676a9b7f',
                extraQueryParameter: 'nux=1',
                cacheLocation: 'localStorage',
            },
            $httpProvider
        );

    }]);
