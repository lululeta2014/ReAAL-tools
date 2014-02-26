var app = angular.module('reaalproxy', [ 'ngResource' ]);

app
		.controller(
				'ListController',
				function($scope, $resource) {
					var Accounts = $resource('/reaal/:id/', {}, {
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
					$scope.newusername="";
					$scope.shownIndex=-1;
					$scope.shown= function(index){
						return index==$scope.shownIndex;
					}
					$scope.show = function(index){
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
												id : $scope.accounts[index].username
											},
											null,
											function(account) {
												$scope.loading--;
												account.complete=true;
												$scope.accounts[index]=account;
											},
											function(err) {
												console.log('error', err);
												$scope.error = 'Error while loading account. See console for more information.';
												$scope.loading--;
											});
						}
					};
					
					$scope.createAccount = function(){
						if ($scope.newusername == ""){
							console.log('error', 'Error while saving account. Username may not be empty.');
							$scope.error = 'Error while saving account. Username may not be empty.';
							return;
						}
						var password = uuid.v4();
						var newAccount = {};
						newAccount.username = $scope.newusername;
						newAccount.password = password;
						
						$scope.saving++;
						Accounts.post( {},
								newAccount,
								function(account){
									$scope.saving--;
									account.complete=true;
									$scope.accounts.push(account);
								},
								function(err){
									console.log('error', err);
									$scope.error = 'Error while saving account:'+err.data;
									$scope.saving--;
								}
						);
					};
					
					$scope.deleteAccount = function(account){
						$scope.accounts = _.without($scope.accounts,account);
						$scope.saving++;
						Accounts.del({ id: account.username },
							function(){
								$scope.saving--;
							},
							function(err){
								$scope.saving--;
								console.log('error', err);
								$scope.error = 'Error while deleting account:'+err.data;
							}
						);
					}
					
					$scope.getAccounts();
				});

