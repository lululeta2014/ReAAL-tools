var app = angular.module('reaalproxy', [ 'ngResource' ]);

app
		.controller(
				'ListController',
				function($scope, $resource) {
					var Accounts = $resource('reaal/:id/', {}, {
						'get' : {
							method : 'GET'
						},
						'post' : {
							method : 'POST'
						},
						'put' : {
							method : 'PUT'
						},
						'del' : {
							method : 'DELETE'
						},
						'list' : {
							method : 'GET',
							isArray : true
						}
					});
					$scope.accounts = [];
					$scope.loading = 0; // Number of load requests currently in
					// progress
					$scope.saving = 0; // Number of save requests currently in
					// progress
					$scope.newemail = "";
					$scope.shownIndex = -1;
					$scope.shown = function(index) {
						return index == $scope.shownIndex;
					}
					$scope.show = function(index) {
						$scope.shownIndex = index;
					}

					$scope.getAccounts = function() {
						$scope.loading++;
						Accounts
								.list(
										{},
										null,
										function(accounts) {
											$scope.accounts = accounts;
											$scope.loading--;
										},
										function(err) {
											console.log('error', err);
											$scope.error = 'Error while retrieving accounts. See console for more information.';
											$scope.loading--;
										});
					};

					$scope.checkLoaded = function(index) {
						if (!$scope.accounts[index].complete) {
							$scope.loading++;
							Accounts
									.get(
											{
												id : $scope.accounts[index].zprId
											},
											null,
											function(account) {
												$scope.loading--;
												account.complete = true;
												$scope.accounts[index] = account;
											},
											function(err) {
												console.log('error', err);
												$scope.error = 'Error while loading account. See console for more information.';
												$scope.loading--;
											});
						}
					};

					function validateEmail(email) {
						var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
						return re.test(email);
					}

					$scope.createAccount = function() {
						if ($scope.newemail == "") {
							console
									.log('error',
											'Error while saving account. Email may not be empty.');
							$scope.error = 'Error while saving account. Email may not be empty.';
							return;
						}
						if (!validateEmail($scope.newemail)) {
							console
									.log('error',
											'Error while saving account. The given email adres is not valid.');
							$scope.error = 'Error while saving account. The given email adres is not valid.';
							return;
						}
						var zprId = $scope.newZprId;
						if (!zprId || zprId == "") {
							zprId = "!"+uuid.v4();
						}

						var newAccount = {};
						newAccount.email = $scope.newemail;
						newAccount.zprId = zprId;
						
						$scope.saving++;
						Accounts.post({}, newAccount, function(account) {
							$scope.saving--;
							account.complete = true;
							$scope.accounts.push(account);
						}, function(err) {
							console.log('error', err);
							$scope.error = 'Error while saving account:'
									+ err.data;
							$scope.saving--;
						});
						$scope.newZprId = "";
						$scope.newemail = "";
					};

					$scope.deleteAccount = function(account) {
						$scope.accounts = _.without($scope.accounts, account);
						$scope.saving++;
						Accounts.del({
							id : account.zprId
						}, function() {
							$scope.saving--;
						}, function(err) {
							$scope.saving--;
							console.log('error', err);
							$scope.error = 'Error while deleting account:'
									+ err.data;
						});
					}
					$scope.getAccounts();
				});
